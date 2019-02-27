/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class CryptoUtils {

    /**
     * Creates public/private key pair using the Elliptic Curve cryptography.
     *
     * @param curveName the name of curve to use
     * @return the public/private key pair
     * @throws NoSuchAlgorithmException if algorithm is not supported
     * @throws NoSuchProviderException if Bouncy Castle is not registered
     * @throws InvalidAlgorithmParameterException if passed curve name is not
     * valid
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
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC", "BC");
        g.initialize(ecSpec, new SecureRandom());
        return g.generateKeyPair();
    }

    /**
     * Creates certificate signing request (CSR) for the passed key pair
     * instance.
     *
     * @param keyPair the public/private key pair
     * @param subject the subject for certificate
     * @param signatureAlgorithm the signature algorith
     * @return the certificate signing request
     * @throws OperatorCreationException if CSR creation fails
     */
    public static PKCS10CertificationRequest createCsr(KeyPair keyPair, X500Principal subject,
            String signatureAlgorithm) throws
            OperatorCreationException {
        PKCS10CertificationRequestBuilder certificationRequestBuilder
                = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
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
}
