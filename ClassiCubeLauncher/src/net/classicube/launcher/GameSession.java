package net.classicube.launcher;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingWorker;
import org.apache.commons.lang3.StringEscapeUtils;

// Base class for service-specific handlers.
// A new single-use GameService object is created for every session.
abstract class GameSession {

    // constructor used by implementations
    protected GameSession(String serviceName, UserAccount account) {
        store = Preferences.userNodeForPackage(this.getClass())
                .node("GameServices")
                .node(serviceName);
        if (account == null) {
            throw new IllegalArgumentException("account may not be null");
        }
        this.account = account;
        cookieJar.removeAll();
    }

    // Asynchronously sign a user in.
    // If "remember" is true, service should attempt to reuse stored credentials (if possible),
    // and store working credentials for next time after signing in.
    public abstract SwingWorker<SignInResult, String> signInAsync(boolean remember);

    // Asynchronously fetches the server list.
    public abstract SwingWorker<ServerInfo[], ServerInfo> getServerListAsync();

    // Gets mppass for given server
    public abstract String getServerPass(ServerInfo server);

    // Gets service site's root URL (for cookie filtering).
    public abstract URI getSiteUri();

    // Gets base skin URL (to pass to the client).
    public abstract String getSkinUrl();

    // Clears all stored cookies
    protected void clearCookies() {
        cookieJar.removeAll();
    }

    // Stores all cookies to Preferences
    protected void storeCookies() {
        try {
            store.clear();
        } catch (BackingStoreException ex) {
            LogUtil.Log(Level.SEVERE, "Error storing session", ex);
        }
        for (HttpCookie cookie : cookieJar.getCookies()) {
            store.put(cookie.getName(), cookie.toString());
        }
    }

    // Loads all cookies from Preferences
    protected void loadCookies() {
        try {
            for (String cookieName : store.keys()) {
                HttpCookie newCookie = new HttpCookie(cookieName, store.get(cookieName, null));
                cookieJar.add(getSiteUri(), newCookie);
            }
        } catch (BackingStoreException ex) {
            LogUtil.Log(Level.SEVERE, "Error loading session", ex);
        }
    }

    // Tries to find a cookie by name. Returns null if not found.
    protected HttpCookie getCookie(String name) {
        List<HttpCookie> cookies = cookieJar.get(getSiteUri());
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    // Checks whether a cookie with the given name is stored.
    protected boolean hasCookie(String name) {
        return (getCookie(name) != null);
    }

    // Encodes a string in a URL-friendly format, for GET or POST
    protected String urlEncode(String str) {
        String enc = StandardCharsets.UTF_8.name();
        try {
            return URLEncoder.encode(str, enc);
        } catch (UnsupportedEncodingException ex) {
            LogUtil.Log(Level.SEVERE, "UrlEncode error: " + ex);
            return null;
        }
    }

    // Decodes an HTML-escaped string
    protected String htmlDecode(String str) {
        return StringEscapeUtils.UNESCAPE_HTML4.translate(str);
    }

    // Initializes the cookie manager
    public static void Init() {
        CookieManager cm = new CookieManager();
        cookieJar = cm.getCookieStore();
        CookieManager.setDefault(cm);
    }
    private static CookieStore cookieJar;
    protected UserAccount account;
    protected Preferences store;
}