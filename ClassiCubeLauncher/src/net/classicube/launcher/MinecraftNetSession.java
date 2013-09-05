package net.classicube.launcher;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;

class MinecraftNetSession extends GameSession {

    private static final String LoginSecureUri = "https://minecraft.net/login",
            LogoutUri = "http://minecraft.net/logout",
            HomepageUri = "http://minecraft.net",
            ServerListUri = "http://minecraft.net/classic/list",
            MigratedAccountMessage = "Your account has been migrated",
            WrongUsernameOrPasswordMessage = "Oops, unknown username or password.",
            authTokenPattern = "<input type=\"hidden\" name=\"authenticityToken\" value=\"([0-9a-f]+)\">",
            loggedInAsPattern = "<span class=\"logged-in\">\\s*Logged in as ([a-zA-Z0-9_\\.]{2,16})",
            serverNamePattern = "<a href=\"/classic/play/([0-9a-f]+)\">([^<]+)</a>",
            otherServerDataPattern = "<td>(\\d+)</td>[^<]*<td>(\\d+)</td>[^<]*<td>([^<]+)</td>.+url\\(/images/flags/([a-z]+).png\\)",
            CookieName = "PLAY_SESSION";
    private static final Pattern authTokenRegex = Pattern.compile(authTokenPattern),
            loggedInAsRegex = Pattern.compile(loggedInAsPattern),
            serverNameRegex = Pattern.compile(serverNamePattern),
            otherServerDataRegex = Pattern.compile(otherServerDataPattern);

    public MinecraftNetSession(UserAccount account) {
        super("MinecraftNetService", account);
        try {
            siteUri = new URI(HomepageUri);
        } catch (URISyntaxException ex) {
            LogUtil.Die("Cannot set siteUri", ex);
        }
    }

    @Override
    public SwingWorker<SignInResult, String> signInAsync(boolean remember) {
        return new MinecraftNetSignInWorker(remember);
    }

    // Asynchronously try signing in our user
    class MinecraftNetSignInWorker extends SwingWorker<SignInResult, String> {

        public MinecraftNetSignInWorker(boolean remember) {
            this.remember = remember;
        }

        @Override
        protected SignInResult doInBackground() throws Exception {
            final String logPrefix = "MinecraftNetSignInWorker.doInBackground: ";
            boolean restoredSession = false;
            try {
                restoredSession = loadSessionCookie(remember);
            } catch (BackingStoreException ex) {
                LogUtil.Log(Level.WARNING, "Error restoring session", ex);
            }

            // "this.publish" can be used to send text status updates to the GUI (not hooked up)
            this.publish("Connecting to Minecraft.net");

            // download the login page
            String loginPage = downloadString(LoginSecureUri);

            // See if we're already logged in
            Matcher loginMatch = loggedInAsRegex.matcher(loginPage);
            if (loginMatch.find()) {
                String actualPlayerName = loginMatch.group(1);
                if (remember && hasCookie(CookieName)
                        && actualPlayerName.equalsIgnoreCase(account.PlayerName)) {
                    // If player is already logged in with the right account: reuse a previous session
                    account.PlayerName = actualPlayerName;
                    LogUtil.Log(Level.INFO, logPrefix + "Restored session for " + account.PlayerName);
                    storeCookies();
                    return SignInResult.SUCCESS;

                } else {
                    // If we're not supposed to reuse session, if old username is different,
                    // or if there is no play session cookie set - relog
                    LogUtil.Log(Level.INFO, logPrefix + "Switching accounts from "
                            + actualPlayerName + " to " + account.PlayerName);
                    downloadString(LogoutUri);
                    clearCookies();
                    loginPage = downloadString(LoginSecureUri);
                }
            }

            // Extract authenticityToken from the login page
            Matcher authTokenMatch = authTokenRegex.matcher(loginPage);
            if (!authTokenMatch.find()) {
                if (restoredSession) {
                    // restoring session failed; log out and retry
                    downloadString(LogoutUri);
                    clearCookies();
                    LogUtil.Log(Level.WARNING,
                            logPrefix + "Unrecognized login form served by minecraft.net; retrying.");
                    
                } else {
                    // something unexpected happened, panic!
                    LogUtil.Log(Level.INFO, loginPage);
                    throw new SignInException("Login failed: Unrecognized login form served by minecraft.net");
                }
            }

            // Built up a login request
            String authToken = authTokenMatch.group(1);
            StringBuilder requestStr = new StringBuilder();
            requestStr.append("username=");
            requestStr.append(urlEncode(account.SignInUsername));
            requestStr.append("&password=");
            requestStr.append(urlEncode(account.Password));
            requestStr.append("&authenticityToken=");
            requestStr.append(urlEncode(authToken));
            if (remember) {
                requestStr.append("&remember=true");
            }
            requestStr.append("&redirect=");
            requestStr.append(urlEncode(HomepageUri));

            // POST our data to the login handler
            this.publish("Signing in...");
            String loginResponse = uploadString(LoginSecureUri, requestStr.toString());

            // Check for common failure scenarios
            if (loginResponse.contains(WrongUsernameOrPasswordMessage)) {
                return SignInResult.WRONG_USER_OR_PASS;
            } else if (loginResponse.contains(MigratedAccountMessage)) {
                return SignInResult.MIGRATED_ACCOUNT;
            }

            // Confirm tha we are now logged in
            Matcher responseMatch = loggedInAsRegex.matcher(loginResponse);
            if (responseMatch.find()) {
                account.PlayerName = responseMatch.group(1);
                return SignInResult.SUCCESS;
            } else {
                LogUtil.Log(Level.INFO, loginResponse);
                throw new SignInException("Login failed: Unrecognized response served by minecraft.net");
            }
        }
        boolean remember;
    }

    @Override
    public SwingWorker<ServerInfo[], ServerInfo> getServerListAsync() {
        return new MinecraftNetGetServerListWorker();
    }

    class MinecraftNetGetServerListWorker extends SwingWorker<ServerInfo[], ServerInfo> {

        @Override
        protected ServerInfo[] doInBackground() throws Exception {
            String serverListString = downloadString(ServerListUri);
            Matcher serverListMatch = serverNameRegex.matcher(serverListString);
            Matcher otherServerDataMatch = otherServerDataRegex.matcher(serverListString);
            ArrayList<ServerInfo> servers = new ArrayList<>();
            // Go through server table, one at a time!
            while (serverListMatch.find()) {
                // Fetch server's basic info
                ServerInfo server = new ServerInfo();
                server.hash = serverListMatch.group(1);
                server.name = htmlDecode(serverListMatch.group(2));
                int rowStart = serverListMatch.end();
                
                // Try getting the rest using another regex
                if (otherServerDataMatch.find(rowStart)) {
                    // this bit doesn't actually work yet (gotta fix my regex)
                    server.players = Integer.parseInt(otherServerDataMatch.group(1));
                    server.maxPlayers = Integer.parseInt(otherServerDataMatch.group(2));
                    String uptimeString = otherServerDataMatch.group(3);
                    try {
                        server.uptime = parseUptime(uptimeString);
                    } catch (IllegalArgumentException ex) {
                        LogUtil.Log(Level.WARNING, "Error parsing server uptime (\""
                                + uptimeString + "\") for " + server.name, ex);
                    }
                    server.flag = otherServerDataMatch.group(4);
                } else {
                    LogUtil.Log(Level.WARNING, "Error passing extended server info for " + server.name);
                }
                servers.add(server);
            }
            // This list is heading off to ServerListScreen (not implemented yet)
            return servers.toArray(new ServerInfo[0]);
        }
    }

    @Override
    public String getServerPass(ServerInfo server) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSkinUrl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getSiteUri() {
        return siteUri;
    }

    // Tries to restore previous session (if possible)
    private boolean loadSessionCookie(boolean remember) throws BackingStoreException {
        final String logPrefix = "MinecraftNetService.loadSessionCookie: ";
        clearCookies();
        if (store.childrenNames().length > 0) {
            if (remember) {
                loadCookies();
                HttpCookie cookie = super.getCookie(CookieName);
                String userToken = "username%3A" + account.SignInUsername + "%00";
                if (cookie != null && cookie.getValue().contains(userToken)) {
                    LogUtil.Log(Level.FINE,
                            logPrefix + "Loaded saved session for " + account.SignInUsername);
                    return true;
                } else {
                    LogUtil.Log(Level.FINE,
                            logPrefix + "Discarded saved session (username mismatch).");
                }
            } else {
                LogUtil.Log(Level.FINE, logPrefix + "Discarded a saved session.");
            }
        } else {
            LogUtil.Log(Level.FINE, logPrefix + "No session saved.");
        }
        return false;
    }

    // Parses Minecraft.net server list's way of showing uptime (e.g. 1s, 1m, 1h, 1d)
    // Returns the number of seconds
    private int parseUptime(String uptime) throws IllegalArgumentException {
        String numPart = uptime.substring(0, uptime.length() - 1);
        char unitPart = uptime.charAt(uptime.length() - 1);
        int number = Integer.parseInt(numPart);
        switch (unitPart) {
            case 's':
                return number;
            case 'm':
                return number * 60;
            case 'h':
                return number * 60 * 60;
            case 'd':
                return number * 60 * 60 * 24;
            default:
                throw new IllegalArgumentException("Invalid date/time parameter.");
        }
    }
    URI siteUri;
}