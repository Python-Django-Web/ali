package ali.HLS;

/**
 * <b>阿里云相关配置参数</b>
 *  项目上线需要修改解密接口URL
 */
public class AliConfig {

    //阿里云accessKeyId
    public static String accessKeyId= "LTAI4FqBBiPQU182LnTcwkCY";
    //阿里云acckeysecret
    public static String acckeysecret = "BQXgyr6PLl5tR2jNG0rHCcOaiBJUxG";
    // 点播服务接入区域
    public static String regionId = "cn-shanghai";
    //标准加密serviceKey
    public static String serviceKey = "a504056e-0b9f-4a6f-b989-3ae87b0844bd";
    //HLS标准解密模板ID
    public static String templateGroupId="4d2a06d69e3aa8633555fd5917ea8190";
    //解密接口url
    public static String decryptKeyUri ="http://172.18.0.206/api/aliyun/video/HlsDecrypt?";

}
