package com.github.webdriverextensions;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyUtils {
    public static HttpHost createProxyFromSettings(Proxy proxySettings) throws MojoExecutionException {
        if (proxySettings == null) {
            return null;
        }
        return new HttpHost(proxySettings.getHost(), proxySettings.getPort());
    }

    static CredentialsProvider createProxyCredentialsFromSettings(Proxy proxySettings) throws MojoExecutionException {
        if (proxySettings.getUsername() == null) {
            return null;
        }
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(proxySettings.getUsername(), proxySettings.getPassword()));

        return credentialsProvider;
    }

    public static void setProxyAuthenticator(final Proxy proxy) {
        Authenticator authenticator = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(proxy.getUsername(),
                        proxy.getPassword().toCharArray()));
            }
        };
        Authenticator.setDefault(authenticator);
    }

    public static Proxy getProxyFromSettings(InstallDriversMojo mojo) throws MojoExecutionException {
        if (mojo.settings == null) {
            return null;
        }

        if (mojo.proxyId != null) {
            for (Proxy proxy : mojo.settings.getProxies()) {
                if (mojo.proxyId.equals(proxy.getId())) {
                    return proxy;
                }
            }
            throw new InstallDriversMojoExecutionException("Configured proxy with id=" + mojo.proxyId + " not found in settings.xml");
        }

        // Get active http/https proxy
        for (Proxy proxy : mojo.settings.getProxies()) {
            if (proxy.isActive() && ("http".equalsIgnoreCase(proxy.getProtocol()) || "https".equalsIgnoreCase(proxy.getProtocol()))) {
                return proxy;
            }
        }

        return null;
    }
}
