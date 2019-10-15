package ali;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.kms.model.v20160120.DecryptRequest;
import com.aliyuncs.kms.model.v20160120.DecryptResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import org.apache.commons.codec.binary.Base64;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class HlsDecryptServer {
    private static DefaultAcsClient client;
    static {
        //KMS的区域，必须与视频对应区域
        String region = "<cn-shanghai";
        //访问KMS的授权AK信息
        String accessKeyId = "LTAI4FqBBiPQU182LnTcwkCY";
        String accessKeySecret = "BQXgyr6PLl5tR2jNG0rHCcOaiBJUxG";
        client = new DefaultAcsClient(DefaultProfile.getProfile(region, accessKeyId, accessKeySecret));
    }
    /**
     * 说明：
     * 1、接收解密请求，获取密文秘钥和令牌Token
     * 2、调用KMS decrypt接口获取明文秘钥
     * 3、将明文秘钥base64decode返回
     */
    public class HlsDecryptHandler implements HttpHandler {
        /**
         * 处理解密请求
         * @param httpExchange
         * @throws IOException
         */
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestMethod = httpExchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(requestMethod)) {
                //校验token的有效性
                String token = getMtsHlsUriToken(httpExchange);
                boolean validRe = validateToken(token);
                if (!validRe) {
                    return;
                }
                //从URL中取得密文密钥
                String ciphertext = getCiphertext(httpExchange);
                if (null == ciphertext)
                    return;
                //从KMS中解密出来，并Base64 decode
                byte[] key = decrypt(ciphertext);
                //设置header
                setHeader(httpExchange, key);
                //返回base64decode之后的密钥
                OutputStream responseBody = httpExchange.getResponseBody();
                responseBody.write(key);
                responseBody.close();
            }
        }
        private void setHeader(HttpExchange httpExchange, byte[] key) throws IOException {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, key.length);
        }
        /**
         * 调用KMS decrypt接口解密，并将明文base64decode
         * @param ciphertext
         * @return
         */
        private byte[] decrypt(String ciphertext) {
            DecryptRequest request = new DecryptRequest();
            request.setCiphertextBlob(ciphertext);
            request.setProtocol(ProtocolType.HTTPS);
            try {
                DecryptResponse response = client.getAcsResponse(request);
                String plaintext = response.getPlaintext();
                //注意：需要base64 decode
                return Base64.decodeBase64(plaintext);
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            }
        }
        /**
         * 校验令牌有效性
         * @param token
         * @return
         */
        private boolean validateToken(String token) {
            if (null == token || "".equals(token)) {
                return false;
            }
            //TODO 业务方实现令牌有效性校验
            return true;
        }
        /**
         * 从URL中获取密文秘钥参数
         * @param httpExchange
         * @return
         */
        private String getCiphertext(HttpExchange httpExchange) {
            URI uri = httpExchange.getRequestURI();
            String queryString = uri.getQuery();
            String pattern = "Ciphertext=(\\w*)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(queryString);
            if (m.find())
                return m.group(1);
            else {
                System.out.println("Not Found Ciphertext Param");
                return null;
            }
        }
        /**
         * 获取Token参数
         *
         * @param httpExchange
         * @return
         */
        private String getMtsHlsUriToken(HttpExchange httpExchange) {
            URI uri = httpExchange.getRequestURI();
            String queryString = uri.getQuery();
            String pattern = "MtsHlsUriToken=(\\w*)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(queryString);
            if (m.find())
                return m.group(1);
            else {
                System.out.println("Not Found MtsHlsUriToken Param");
                return null;
            }
        }
    }
    /**
     * 服务启动
     *
     * @throws IOException
     */
    private void serviceBootStrap() throws IOException {
        HttpServerProvider provider = HttpServerProvider.provider();
        //监听端口9999,能同时接受30个请求
        HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(9999), 30);
        httpserver.createContext("/", new HlsDecryptHandler());
        httpserver.start();
        System.out.println("hls decrypt server started");
    }
    public static void main(String[] args) throws IOException {
       /* HlsDecryptServer server = new HlsDecryptServer();
        server.serviceBootStrap();*/
    	String b = "cccbccaaaabaaacccbcc";
    	String a ="aaaabaaa";
    	 Pattern r = Pattern.compile(a);
    	 Matcher m = r.matcher(b);
    	System.out.println( m.group(1));
    	
    	
    }
}
