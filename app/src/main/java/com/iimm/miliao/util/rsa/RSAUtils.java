package com.iimm.miliao.util.rsa;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * @title: RSAUtils
 * @fileName: RSAUtils.java
 * @author: 栗超
 * @date:2016年8月22日 上午11:32:14
 * @description:RSA公钥/私钥/签名工具包 <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 */
public class RSAUtils {

    /**
     * 加密算法RSA
     */
//    public static final String KEY_ALGORITHM = "RSA";
    public static final String KEY_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * 签名长度
     */
    public static final int KEY_SIZE = 1024;

    /**
     * 获取公钥的key
     */
    public static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDg/CxgoI8m6EXa6QJsleT1k+X6Cg2cGC2aS9il05kW7zfIgoIUwqGO6EXlcIWsRFgJQWvxS94vtbbCWqC9Os4SvfazikT8TmyQtCNnfGSqM7eZKql/jR6XAGBEN4OIQOrtb8GdO4PSpi5NhBziaGEGeSC4LmmolFic9Fm6FHYD4wIDAQAB";
    /**
     * 获取私钥的key
     */
    public static String PRIVATE_KEY = "";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 生成密钥对(公钥和私钥)
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param dataStr 已加密数据
     * @return
     * @throws Exception
     */
    public static String sign(String dataStr) throws Exception {
        byte[] data = new BASE64Decoder().decodeBuffer(dataStr);
        RSAPrivateKey privateKeyRsa = RSAUtils.loadPrivateKey(PRIVATE_KEY);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
//        signature.initSign(PRIVATE_KEY_RSA);
        signature.initSign(privateKeyRsa);
        signature.update(data);
        return new BASE64Encoder().encodeBuffer(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param dataStr 已加密数据
     * @param sign    数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(String dataStr, String sign) throws Exception {
        byte[] data = new BASE64Decoder().decodeBuffer(dataStr);
        RSAPublicKey publicKeyRsa = RSAUtils.loadPublicKey(PUBLIC_KEY);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKeyRsa);
        signature.update(data);
        return signature.verify(new BASE64Decoder().decodeBuffer(sign));
    }

    /**
     * 私钥解密
     *
     * @param encryptedData 已加密数据
     * @param privateKeyRsa
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, RSAPrivateKey privateKeyRsa) throws Exception {
//    	if(PRIVATE_KEY_RSA == null){
//    		throw new Exception("解密私钥为空，请设置");
//    	}
        if (privateKeyRsa == null) {
            throw new Exception("解密私钥为空，请设置");
        }
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY_RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKeyRsa);
            return RSAUtils.partitionAlgorithm(encryptedData, cipher, MAX_DECRYPT_BLOCK);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * 公钥解密
     *
     * @param encryptedData 已加密数据
     * @param publicKeyRsa
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, RSAPublicKey publicKeyRsa) throws Exception {
//    	if(PUBLIC_KEY_RSA == null){
//    		throw new Exception("解密公钥为空，请设置");
//    	}
        if (publicKeyRsa == null) {
            throw new Exception("解密公钥为空，请设置");
        }
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
//        	cipher.init(Cipher.DECRYPT_MODE, PUBLIC_KEY_RSA);
            cipher.init(Cipher.DECRYPT_MODE, publicKeyRsa);
            return RSAUtils.partitionAlgorithm(encryptedData, cipher, MAX_DECRYPT_BLOCK);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * 公钥加密
     *
     * @param data         源数据
     * @param publicKeyRsa
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKeyRsa) throws Exception {
        // 对数据加密 
//    	if(PUBLIC_KEY_RSA == null){
//    		throw new Exception("加密公钥为空，请设置");
//    	}
        if (publicKeyRsa == null) {
            throw new Exception("加密公钥为空，请设置");
        }
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
//        	cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKeyRsa);
            return RSAUtils.partitionAlgorithm(data, cipher, MAX_ENCRYPT_BLOCK);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 私钥加密
     *
     * @param data          源数据
     * @param privateKeyRsa
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, RSAPrivateKey privateKeyRsa) throws Exception {
//    	if(PRIVATE_KEY_RSA == null){
//    		throw new Exception("加密私钥为空，请设置");
//    	}
        if (privateKeyRsa == null) {
            throw new Exception("加密私钥为空，请设置");
        }
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
//        	cipher.init(Cipher.ENCRYPT_MODE, PRIVATE_KEY_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, privateKeyRsa);
            return RSAUtils.partitionAlgorithm(data, cipher, MAX_ENCRYPT_BLOCK);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 分段算法
     *
     * @param data   数据
     * @param cipher 加解密类库
     * @return
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] partitionAlgorithm(byte[] data, Cipher cipher, int maxBlock) throws IOException, BadPaddingException, IllegalBlockSizeException {
        int inputLen = data.length;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > maxBlock) {
                    cache = cipher.doFinal(data, offSet, maxBlock);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * maxBlock;
            }
            byte[] encryptedData = out.toByteArray();
//            out.close();
            return encryptedData;
        } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
            throw e;
        }
    }

    /**
     * 获取私钥
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return new BASE64Encoder().encodeBuffer(key.getEncoded());
    }

    /**
     * 获取公钥
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return new BASE64Encoder().encodeBuffer(key.getEncoded());
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
//            RSAUtils.PUBLIC_KEY_RSA= (RSAPublicKey) keyFactory.generatePublic(keySpec);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (IOException e) {
            throw new Exception("公钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从字符串中加载私钥
     *
     * @param privateKeyStr 私钥数据字符串
     * @throws Exception 加载私钥时产生的异常
     */
    public static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            RSAUtils.PRIVATE_KEY_RSA = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new Exception("私钥非法");
        } catch (IOException e) {
            throw new Exception("私钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 私钥解密后的字符串
     *
     * @param base64String
     * @return
     * @throws Exception
     */
    public static String decryptPrivateWithBase64(String base64String) throws Exception {
        RSAPrivateKey privateKeyRsa = RSAUtils.loadPrivateKey(PRIVATE_KEY);
        byte[] binaryData = decryptByPrivateKey(new BASE64Decoder().decodeBuffer(base64String), privateKeyRsa);
        String string = new String(binaryData);
        return string;
    }

    /**
     * 公钥加密后的字符串
     *
     * @param string
     * @return
     * @throws Exception
     */
    public static String encryptPublicWithBase64(String string) throws Exception {
        RSAPublicKey publicKeyRsa = RSAUtils.loadPublicKey(PUBLIC_KEY);
        byte[] binaryData = encryptByPublicKey(string.getBytes("utf-8"), publicKeyRsa);
        String base64String = new BASE64Encoder().encodeBuffer(binaryData);
        return base64String;
    }

    /**
     * 公钥解密后的字符串
     *
     * @param base64String
     * @return
     * @throws Exception
     */
    public static String decryptPublicWithBase64(String base64String) throws Exception {
        RSAPublicKey publicKeyRsa = RSAUtils.loadPublicKey(PUBLIC_KEY);
        byte[] binaryData = decryptByPublicKey(new BASE64Decoder().decodeBuffer(base64String), publicKeyRsa);
        String string = new String(binaryData);
        return string;
    }

    /**
     * 私钥加密后的字符串
     *
     * @param string
     * @return
     * @throws Exception
     */
    public static String encryptPrivateWithBase64(String string) throws Exception {
        RSAPrivateKey privateKeyRsa = RSAUtils.loadPrivateKey(PRIVATE_KEY);
        byte[] binaryData = encryptByPrivateKey(string.getBytes("utf-8"), privateKeyRsa);
        String base64String = new BASE64Encoder().encodeBuffer(binaryData);
        return base64String;
    }

}  