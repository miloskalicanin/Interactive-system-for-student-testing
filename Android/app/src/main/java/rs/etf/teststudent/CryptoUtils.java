package rs.etf.teststudent;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.cert.CertIOException;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

/**
 * Created by milos on 19.4.17..
 */

public class CryptoUtils {
    private static final long VALIDITY_PERIOD = 1000L * 60L * 60L * 24L * 180L;

    /**
     * Creates the X509v3 client certificate for passed public key using the CA
     * (certificate authority) private key and certificate.
     *
     * @param publicKey          the client public key
     * @param caKey              the CA private key
     * @param caCert             the CA certificate
     * @param subject            the subject of new certificate
     * @param signatureAlgorithm the signature algorithm validating the certificate
     * @return X509 certificate
     * @throws CertIOException           the cert io exception
     * @throws NoSuchAlgorithmException  the no such algorithm exception
     * @throws OperatorCreationException the operator creation exception
     * @throws CertificateException      the certificate exception
     */
    public static X509Certificate createClientCertificate(PublicKey publicKey, PrivateKey caKey,
                                                          X509CertificateHolder caCert, X500Name subject, String signatureAlgorithm)
            throws CertIOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException {
        X509v3CertificateBuilder certBldr = new JcaX509v3CertificateBuilder(caCert.getIssuer(), BigInteger.valueOf(1),
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + VALIDITY_PERIOD), subject,
                publicKey);
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBldr.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(caCert))
                .addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(publicKey))
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(false)).addExtension(
                Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm).setProvider("BC").build(caKey);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBldr.build(signer));
    }

    /**
     * Create client certificate from certificate signing request (CSR).
     *
     * @param csr                the certificate signing request
     * @param caKey              the certificate authority key
     * @param caCert             the certificate authority certificate
     * @param signatureAlgorithm the signature algorithm validating the certificate
     * @param subject            optional parameter, overrides certificate subject DN defined
     *                           in CSR request
     * @return the x 509 certificate
     * @throws CertIOException           the cert io exception
     * @throws NoSuchAlgorithmException  the no such algorithm exception
     * @throws OperatorCreationException the operator creation exception
     * @throws CertificateException      the certificate exception
     * @throws InvalidKeyException       the invalid key exception
     */
    public static X509Certificate createClientCertificateFromCsr(PKCS10CertificationRequest csr, PrivateKey caKey,
                                                                 X509CertificateHolder caCert, String signatureAlgorithm, String subject) throws CertIOException,
            NoSuchAlgorithmException, OperatorCreationException, CertificateException, InvalidKeyException {
        JcaPKCS10CertificationRequest jcaCsr = new JcaPKCS10CertificationRequest(csr);
        X509v3CertificateBuilder certBldr = new JcaX509v3CertificateBuilder(caCert.getIssuer(), BigInteger.valueOf(1),
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                subject != null ? new X500Name(subject) : jcaCsr.getSubject(), jcaCsr.getPublicKey());
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBldr.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(caCert))
                .addExtension(Extension.subjectKeyIdentifier, false,
                        extUtils.createSubjectKeyIdentifier(jcaCsr.getSubjectPublicKeyInfo()))
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(false)).addExtension(
                Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm).setProvider("BC").build(caKey);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBldr.build(signer));
    }

    /**
     * Get certificate fingerprint.
     *
     * @param x509Certificate the x509 certificate
     * @return the certificate fingerprint
     */
    public static byte[] getCertificateFingerprint(X509Certificate x509Certificate)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        // Encoded version of certificate (it is in ASN.1 DER format i.e. one of
        // PKCS#xx formats)
        byte[] derBytes = x509Certificate.getEncoded();
        md.update(derBytes);

        // bytes of hash value
        return md.digest();
    }

    /**
     * Creates public/private key pair using the Elliptic Curve cryptography.
     *
     * @param curveName the name of curve to use
     * @return the public/private key pair
     * @throws NoSuchAlgorithmException           if algorithm is not supported
     * @throws NoSuchProviderException            if Bouncy Castle is not registered
     * @throws InvalidAlgorithmParameterException if passed curve name is not valid
     */
    public static KeyPair createEcKeyPair(String curveName)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        X9ECParameters ecP = CustomNamedCurves.getByName(curveName);
        ECParameterSpec ecSpec = new ECNamedCurveParameterSpec(
                curveName,
                ecP.getCurve(),
                ecP.getG(),
                ecP.getN(),
                ecP.getH(),
                ecP.getSeed());
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC",
                new BouncyCastleProvider());
        g.initialize(ecSpec, new SecureRandom());
        return g.generateKeyPair();
    }

    /**
     * Creates certificate signing request (CSR) for the passed key pair
     * instance.
     *
     * @param keyPair            the public/private key pair
     * @param subject            the subject for certificate
     * @param signatureAlgorithm the signature algorith
     * @return the certificate signing request
     * @throws OperatorCreationException if CSR creation fails
     */
    public static PKCS10CertificationRequest createCsr(KeyPair keyPair, X500Principal subject,
                                                       String signatureAlgorithm) throws OperatorCreationException {
        PKCS10CertificationRequestBuilder certificationRequestBuilder = new JcaPKCS10CertificationRequestBuilder(
                subject, keyPair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
        return certificationRequestBuilder.build(csBuilder.build(keyPair.getPrivate()));
    }

    public static byte[] csrBytes(PKCS10CertificationRequest csr) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(result, true);
        JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(out);
        jcaPEMWriter.writeObject(csr);
        jcaPEMWriter.flush();

        return result.toByteArray();
    }

    /**
     * Read OpenSSL generated Elliptic Curve private/public key (PEM encoding).
     *
     * @param filePath path to key file
     * @return the parse key pair
     * @throws IOException if file parsing failed
     */

    public static KeyPair readOpenSslEcKey(String filePath) throws IOException {
        // ca keys
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {

            // ec params (not needed)
            //Object ecParams = pemParser.readObject();
            // key pair
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getKeyPair((PEMKeyPair) pemParser.readObject());
        }
    }

    /**
     * Encodes certificate (PEM encoding).
     *
     * @param x509Certificate the certificate to encode
     * @return the PEM encoding byte array
     * @throws IOException if encoding fails
     */
    public static byte[] encodeCertificate(X509Certificate x509Certificate) throws IOException {
        StringWriter out = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(out)) {
            pemWriter.writeObject(x509Certificate);
        }
        return out.toString().getBytes();
    }

    /**
     * Decodes certificate (from PEM encoding)
     *
     * @param encoded encoded certificate bytes
     * @return the decoded certificate
     * @throws IOException if decoding fails
     */
    public static X509CertificateHolder decodeCertificate(byte[] encoded) throws IOException {
        X509CertificateHolder caCert;
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(encoded)))) {
            caCert = (X509CertificateHolder) pemParser.readObject();
        }
        return caCert;
    }

    /**
     * Read certificate from file.
     *
     * @param certPath the cert path
     * @return the x509 certificate
     * @throws IOException if reading fails
     */
    public static X509CertificateHolder readCertificateFromFile(String certPath) throws IOException {
        X509CertificateHolder caCert;
        try (PEMParser pemParser = new PEMParser(new FileReader(certPath))) {
            caCert = (X509CertificateHolder) pemParser.readObject();
        }
        return caCert;
    }

    public static KeyPair readKeyPair(byte[] encodedKeyPair) throws IOException {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(encodedKeyPair)))) {
            // key pair
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getKeyPair((PEMKeyPair) pemParser.readObject());
        }
    }

    /*
     * public static byte[] getCertificateFingerprint(Authentication
     * authentication) { byte[] certificateFingerprint = null; try { Object
     * principal = authentication.getCredentials(); if (principal instanceof
     * X509CertificateHolder) { X509CertificateHolder holder =
     * (X509CertificateHolder) principal; X509Certificate certificate = new
     * JcaX509CertificateConverter().setProvider("BC") .getCertificate(holder);
     * certificateFingerprint =
     * CryptoUtils.getCertificateFingerprint(certificate); } } catch (Exception
     * e) { } return certificateFingerprint; }
     */
}