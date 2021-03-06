/*
 * Copyright 2017 Long Term Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ltsllc.miranda;

import com.ltsllc.miranda.http.HttpServer;
import com.ltsllc.miranda.http.JettyHttpServer;
import com.ltsllc.miranda.mina.MinaNetwork;
import com.ltsllc.miranda.mina.MinaNetworkListener;
import com.ltsllc.miranda.miranda.Miranda;
import com.ltsllc.miranda.miranda.MirandaPanicPolicy;
import com.ltsllc.miranda.miranda.PanicPolicy;
import com.ltsllc.miranda.network.Network;
import com.ltsllc.miranda.network.NetworkListener;
import com.ltsllc.miranda.property.MirandaProperties;
import com.ltsllc.miranda.util.Utils;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Based on the values of the properties, this class knows which classes to
 * build.
 */
public class MirandaFactory {
    private static Logger logger = Logger.getLogger(MirandaFactory.class);

    private MirandaProperties properties;
    private String keystorePassword;
    private String truststorePassword;
    private KeyStore keyStore;
    private KeyStore trustStore;

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public MirandaProperties getProperties() {
        return properties;
    }

    public MirandaFactory (MirandaProperties properties, String keystorePassword, String truststorePassword) {
        this.properties = properties;
        this.keystorePassword = keystorePassword;
        this.truststorePassword = truststorePassword;
    }

    public void getKeyStores () {
        try {
            String filename = getProperties().getProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE);
            KeyStore keyStore = Utils.loadKeyStore(filename, getKeystorePassword());
            setKeyStore(keyStore);

            filename = getProperties().getProperty(MirandaProperties.PROPERTY_TRUST_STORE_FILENAME);
            keyStore = Utils.loadKeyStore(filename, getTruststorePassword());
            setTrustStore(keyStore);
        } catch (GeneralSecurityException | IOException e) {
            StartupPanic startupPanic = new StartupPanic("Exception trying to get keystores", e,
                    StartupPanic.StartupReasons.ExceptionLoadingKeystore);
            Miranda.panicMiranda(startupPanic);
        }
    }

    public NetworkListener buildNetworkListener (KeyStore keyStore, KeyStore trustStore) {
        int port = getProperties().getIntProperty(MirandaProperties.PROPERTY_CLUSTER_PORT);


        return new MinaNetworkListener(port, keyStore, getKeystorePassword(), trustStore);
    }

    public Network buildNetwork (KeyStore keyStore, KeyStore trustStore) throws MirandaException {
        return new MinaNetwork(keyStore, trustStore, getKeystorePassword());
    }

    public SslContext buildNettyClientSslContext () throws IOException, GeneralSecurityException {
        String filename = getProperties().getProperty(MirandaProperties.PROPERTY_TRUST_STORE_FILENAME);

        return Utils.createClientSslContext(filename, getTruststorePassword());
    }

    public void checkProperty (String name, String value) throws MirandaException {
        if (null == value || value.equals("")) {
            String message = "No or empty value for property " + name;
            logger.error(message);
            throw new MirandaException(message);
        }
    }


    public SslContext buildServerSslContext() throws MirandaException {
        MirandaProperties properties = Miranda.properties;
        MirandaProperties.EncryptionModes encryptionMode = properties.getEncryptionModeProperty(MirandaProperties.PROPERTY_ENCRYPTION_MODE);
        SslContext sslContext = null;

        switch (encryptionMode) {
            case LocalCA: {
                sslContext = buildLocalCaServerSslContext();
                break;
            }

            case RemoteCA: {
                sslContext = buildRemoteCaServerContext();
                break;
            }
        }

        return sslContext;
    }

    public SslContext buildLocalCaServerSslContext () throws MirandaException {
        /*
        MirandaProperties properties = Miranda.properties;

        String serverKeyStoreFilename = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE);
        checkProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE, serverKeyStoreFilename);

        String serverKeyStorePassword = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_PASSWORD);
        checkProperty(MirandaProperties.PROPERTY_KEYSTORE_PASSWORD, serverKeyStorePassword);

        String serverKeyStoreAlias = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_ALIAS);
        checkProperty(MirandaProperties.PROPERTY_KEYSTORE_ALIAS, serverKeyStoreAlias);

        String trustStoreFilename = properties.getProperty(MirandaProperties.PROPERTY_TRUST_STORE);
        checkProperty(MirandaProperties.PROPERTY_TRUST_STORE, trustStoreFilename);

        String trustStorePassword = properties.getProperty(MirandaProperties.PROPERTY_TRUST_STORE_PASSWORD);
        checkProperty(MirandaProperties.PROPERTY_TRUST_STORE_PASSWORD, trustStorePassword);

        String trustStoreAlias = properties.getProperty(MirandaProperties.PROPERTY_TRUST_STORE_ALIAS);
        checkProperty(MirandaProperties.PROPERTY_TRUST_STORE_ALIAS, trustStoreAlias);

        try {
            return Utils.createServerSslContext(serverKeyStoreFilename, serverKeyStorePassword, serverKeyStoreAlias,
                    trustStoreFilename, trustStorePassword, trustStoreAlias);
        } catch (IOException | GeneralSecurityException e) {
            throw new MirandaException("Exception trying to create server SSL context", e);
        }
        */
        return null;
    }


    public SslContext buildRemoteCaServerContext () throws MirandaException {
        /*
        try {
            MirandaProperties properties = Miranda.properties;

            String serverKeyStoreFilename = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE);
            checkProperty(MirandaProperties.PROPERTY_KEYSTORE, serverKeyStoreFilename);

            String serverKeyStorePassword = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_PASSWORD);
            checkProperty(MirandaProperties.PROPERTY_KEYSTORE_PASSWORD, serverKeyStorePassword);

            String serverKeyStoreAlias = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_ALIAS);
            checkProperty(MirandaProperties.PROPERTY_KEYSTORE_ALIAS, serverKeyStoreAlias);

            String certificateKeyStoreFilename = properties.getProperty(MirandaProperties.PROPERTY_TRUST_STORE);
            checkProperty(MirandaProperties.PROPERTY_TRUST_STORE, certificateKeyStoreFilename);

            String certificateKeyStorePassword = properties.getProperty(MirandaProperties.PROPERTY_CERTIFICATE_PASSWORD);
            checkProperty(MirandaProperties.PROPERTY_CERTIFICATE_PASSWORD, certificateKeyStorePassword);

            String certificateKeyStoreAlias = properties.getProperty(MirandaProperties.PROPERTY_CERTIFICATE_ALIAS);
            checkProperty(MirandaProperties.PROPERTY_CERTIFICATE_ALIAS, certificateKeyStoreAlias);

            java.security.PrivateKey key = Utils.loadKey(serverKeyStoreFilename, serverKeyStorePassword, serverKeyStoreAlias);
            X509Certificate certificate = Utils.loadCertificate(certificateKeyStoreFilename, certificateKeyStorePassword, certificateKeyStoreAlias);

            return SslContextBuilder
                    .forServer(key, certificate)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new MirandaException("Exception trying to create SSL context", e);
        }

*/
        return null;
    }

    private static final String JETTY_BASE = "jetty.base";
    private static final String JETTY_HOME = "jetty.home";
    private static final String JETTY_TAG = "jetty.tag.version";
    private static final String DEFAULT_JETTY_TAG = "master";


    public HttpServer buildHttpServer () throws MirandaException {
        MirandaProperties.WebSevers whichServer = getProperties().getHttpServerProperty(MirandaProperties.PROPERTY_HTTP_SERVER);
        int httpPort = getProperties().getIntProperty(MirandaProperties.PROPERTY_HTTP_PORT);
        int sslPort = getProperties().getIntProperty(MirandaProperties.PROPERTY_HTTP_SSL_PORT);
        String httpBase = getProperties().getProperty(MirandaProperties.PROPERTY_HTTP_BASE);

        HttpServer httpServer = null;

        switch (whichServer) {
            default: {
                httpServer = buildJetty(httpPort, sslPort, httpBase);
                break;
            }
        }

        return httpServer;
    }


    public HttpServer buildJetty (int httpPort, int sslPort, String httpBase) throws MirandaException {
        try {
            MirandaProperties properties = Miranda.properties;

            //
            // jetty wants some properties defined
            //
            File file = new File(httpBase);
            String base = file.getCanonicalPath();

            properties.setProperty(JETTY_BASE, base);
            properties.setProperty(JETTY_HOME, base);
            properties.setProperty(JETTY_TAG, DEFAULT_JETTY_TAG);
            properties.updateSystemProperties();

            Server jetty = new Server();


            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
            resourceHandler.setResourceBase(base);

            HandlerCollection handlerCollection = new HandlerCollection(true);
            handlerCollection.addHandler(resourceHandler);

            jetty.setHandler(handlerCollection);

            String serverKeyStoreFilename = properties.getProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE);
            checkProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE, serverKeyStoreFilename);

            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(serverKeyStoreFilename);
            sslContextFactory.setKeyStorePassword(getKeystorePassword());
            sslContextFactory.setKeyManagerPassword(getKeystorePassword());

            ServerConnector sslConnector = new ServerConnector(jetty,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https));
            sslConnector.setPort(sslPort);

            ServerConnector connector = new ServerConnector(jetty);
            connector.setPort(httpPort);

            jetty.setConnectors(new Connector[] { sslConnector, connector });

            HttpServer httpServer = new JettyHttpServer(jetty, handlerCollection);
            httpServer.start(); // this starts the HttpServer instance not jetty

            return httpServer;
        } catch (Exception e) {
            throw new MirandaException("Exception trying to setup http server", e);
        }
    }

    public SSLContext buildServerSSLContext () {
        MirandaProperties.EncryptionModes mode = getProperties().getEncryptionModeProperty(MirandaProperties.PROPERTY_ENCRYPTION_MODE);
        SSLContext sslContext = null;

        switch (mode) {
            case LocalCA: {
                sslContext = buildLocalCaServerSSLContext();
                break;
            }

            case RemoteCA: {
                sslContext = buildRemoteCaServerSSLContext();
                break;
            }

            default: {
                StartupPanic startupPanic = new StartupPanic("Unrecognized encryption mode: " + mode,
                        StartupPanic.StartupReasons.UnrecognizedEncryptionMode);
                Miranda.panicMiranda(startupPanic);
                break;
            }
        }

        return sslContext;
    }

    public SSLContext buildRemoteCaServerSSLContext () {
        SSLContext sslContext = null;

        try {
            String keyStoreFilename = getProperties().getProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE);
            KeyStore keyStore = Utils.loadKeyStore(keyStoreFilename, getKeystorePassword());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, getKeystorePassword().toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        } catch (GeneralSecurityException | IOException e) {
            Panic panic = new Panic ("Exception while trying to create SSL context", e,
                    Panic.Reasons.ExceptionCreatingSslContext);

            Miranda.getInstance().panic(panic);
        }

        return sslContext;
    }

    public SSLContext buildLocalCaServerSSLContext () {
        SSLContext sslContext = null;

        try {
            String trustStoreFilename = getProperties().getProperty(MirandaProperties.PROPERTY_TRUST_STORE_FILENAME);
            KeyStore trustKeyStore = Utils.loadKeyStore(trustStoreFilename, getTruststorePassword());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustKeyStore);

            String keyStoreFilename = getProperties().getProperty(MirandaProperties.PROPERTY_KEYSTORE_FILE);
            KeyStore keyStore = Utils.loadKeyStore(keyStoreFilename, getKeystorePassword());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, getKeystorePassword().toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (GeneralSecurityException | IOException e) {
            Panic panic = new Panic ("Exception while trying to create SSL context", e,
                    Panic.Reasons.ExceptionCreatingSslContext);

            Miranda.getInstance().panic(panic);
        }

        return sslContext;
    }

    public PanicPolicy buildPanicPolicy () {
        int maxPanics = getProperties().getIntProperty(MirandaProperties.PROPERTY_PANIC_LIMIT);
        long timeout = getProperties().getLongProperty(MirandaProperties.PROPERTY_PANIC_TIMEOUT, MirandaProperties.DEFAULT_PANIC_TIMEOUT);

        return new MirandaPanicPolicy(maxPanics, timeout, Miranda.getInstance(), Miranda.timer);
    }

    public Network buildNettyNetwork () {
        throw new IllegalStateException("not impelmented");
    }

    public Network buildSocketNetwork () {
        throw new IllegalStateException("not impelmented");
    }

    public NetworkListener buildNettyNetworkListener () {
        throw new IllegalStateException("not impelmented");
    }

    public NetworkListener buildSocketNetworkListener () {
        throw new IllegalStateException("not impelmented");
    }

    public NetworkListener buildNewNetworkListener () {
        int port = getProperties().getIntegerProperty(MirandaProperties.PROPERTY_CLUSTER_PORT);
        MirandaProperties.EncryptionModes mode = getProperties().getEncryptionModeProperty(MirandaProperties.PROPERTY_ENCRYPTION_MODE);
        return new MinaNetworkListener(port, getKeyStore(), getKeystorePassword(), getTrustStore());
    }
}
