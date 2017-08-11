
//Written by Mazda Marvasti: AppLariat Corp.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UrlCalls {
	private final static String apl_api_base_url = "$APL_API_BASE_URL";
	
	public static boolean isEnvVarsSet() {
		if (System.getenv("APL_SVC_USERNAME")==null || (System.getenv("APL_SVC_PASSWORD")==null)) {
			return false;
		} else {
			return true;
		}
	}
	public static String replaceApiHostPlaceholder(String url) {
		if(url.contains(apl_api_base_url)) {
			if (System.getenv("APL_API_BASE_URL")!=null) {
				return url.replace(apl_api_base_url, System.getenv("APL_API_BASE_URL"));
			} else {
				return "https://api.applariat.io/v1";
			}
		} else {
			return "https://api.applariat.io/v1";
		}	
	}
	
	private static URL getCustomConnection(String urlString) {
		// Create a trust manager that does not validate certificate chains
		X509TrustManager[] trustAllCerts = new X509TrustManager[] { 
		    new X509TrustManager() {     
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
		            return new X509Certificate[0];
		        } 
		        public void checkClientTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		            } 
		        public void checkServerTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    } 
		}; 

		// Install the all-trusting trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL"); 
		    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} 
		// Now you can access an https URL without having the certificate in the truststore
		try { 
		    URL url = new URL(urlString);
		    return url;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public static String urlConnectRequestToken(String apiUrl, String authStringEnc) {
		String baseUrl = apiUrl + "/request_token";
		HttpsURLConnection connection = null;
		URL urlCon = getCustomConnection(baseUrl);
		if (urlCon==null) { return null; }
		try {
			connection = (HttpsURLConnection)urlCon.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			InputStream response = connection.getInputStream();
			String responseString = getStringFromInputStream(response);

			JSONObject jobj = (JSONObject) new JSONParser().parse(responseString);
	        JSONObject authentication = (JSONObject) jobj.get("authentication");
	        String access_token = (String) authentication.get("access_token");
	        
			return access_token; 
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("URL: "+baseUrl);
			if (connection ==null) {
				System.out.println("Failed url="+baseUrl+"      For reason="+e.toString());
			} else {
				InputStream error = connection.getErrorStream();
				if (error!=null) {
					String errorString = getStringFromInputStream(error);
					System.out.println("Failed url="+baseUrl+"    with message="+errorString);
				} else {
					System.out.println("Failed url="+baseUrl+"    For reason="+e.toString());
				}
			}
			return null;
		}
	}
	
	// this method uses the POST method to fetch data from a URL
	public static String urlConnectPost(String baseUrl, String query, String jwtToken) {
		String charset = "UTF-8";
		HttpURLConnection connection = null;
		try {
			String authorization = "Bearer " + jwtToken;
			
			connection = (HttpURLConnection)getCustomConnection(baseUrl).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept",  "application/json");
			connection.setRequestProperty("Content-Type",  "application/json");
			connection.setRequestProperty  ("Authorization", authorization);
			OutputStream output = connection.getOutputStream();
			output.write(query.getBytes(charset));
			InputStream response = connection.getInputStream();
			String responseString = getStringFromInputStream(response);
			return responseString; 
		} catch (Exception e) {
			e.printStackTrace();
			if (connection ==null) {
				System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
			} else {
				InputStream error = connection.getErrorStream();
				if (error!=null) {
					String errorString = getStringFromInputStream(error);
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  with message="+errorString);
				} else {
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
				}
			}
			return null;
		}
	}
	
	// this method uses the GET method to fetch data from a URL
	public static String urlConnectGet(String baseUrl, String query, String jwtToken) {
		HttpURLConnection connection = null;
		try {
			String authorization = "Bearer " + jwtToken;
			
			if (query!=null && query.length()>0) {
				connection = (HttpURLConnection)getCustomConnection(baseUrl+"?"+query).openConnection();
			} else {
				connection = (HttpURLConnection)getCustomConnection(baseUrl).openConnection();
			}
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept-Charset",  "application/json");
			connection.setRequestProperty("Content-Type",  "application/json");
			connection.setRequestProperty  ("Authorization", authorization);
			InputStream response = connection.getInputStream();
			String responseString = getStringFromInputStream(response);
			return responseString; 
		} catch (Exception e) {
			e.printStackTrace();
			if (connection ==null) {
				System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
			} else {
				InputStream error = connection.getErrorStream();
				if (error!=null) {
					String errorString = getStringFromInputStream(error);
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  with message="+errorString);
				} else {
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
				}
			}
			return null;
		}
	}
	
	// this method uses the DELETE method to fetch data from a URL
	public static String urlConnectDelete(String baseUrl, String query, String jwtToken) {
		HttpURLConnection connection = null;
		try {
			String authorization = "Bearer " + jwtToken;
			
			if (query!=null && query.length()>0) {
				connection = (HttpURLConnection)getCustomConnection(baseUrl+"?"+query).openConnection();
			} else {
				connection = (HttpURLConnection)getCustomConnection(baseUrl).openConnection();
			}
			connection.setRequestMethod("DELETE");
			connection.setRequestProperty("Accept-Charset",  "application/json");
			connection.setRequestProperty("Content-Type",  "application/json");
			connection.setRequestProperty  ("Authorization", authorization);
			InputStream response = connection.getInputStream();
			String responseString = getStringFromInputStream(response);
			return responseString; 
		} catch (Exception e) {
			e.printStackTrace();
			if (connection ==null) {
				System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
			} else {
				InputStream error = connection.getErrorStream();
				if (error!=null) {
					String errorString = getStringFromInputStream(error);
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  with message="+errorString);
				} else {
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
				}
			}
			return null;
		}
	}
	// this method uses the PUT method to fetch data from a URL
	public static String urlConnectPut(String baseUrl, String query, String jwtToken) {
		String charset = "UTF-8";
		HttpURLConnection connection = null;
		try {
			String authorization = "Bearer " + jwtToken;
			
			connection = (HttpURLConnection)getCustomConnection(baseUrl).openConnection();
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept",  "application/json");
			connection.setRequestProperty("Content-Type",  "application/json");
			connection.setRequestProperty  ("Authorization", authorization);
			OutputStream output = connection.getOutputStream();
			output.write(query.getBytes(charset));
			InputStream response = connection.getInputStream();
			String responseString = getStringFromInputStream(response);
			return responseString; 
		} catch (Exception e) {
			e.printStackTrace();
			if (connection ==null) {
				System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
			} else {
				InputStream error = connection.getErrorStream();
				if (error!=null) {
					String errorString = getStringFromInputStream(error);
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  with message="+errorString);
				} else {
					System.out.println("Failed url="+baseUrl+"    With query="+query+"  For reason="+e.toString());
				}
			}
			return null;
		}
	}
	// convert InputStream to String
	public static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	public static String decode(String val) {
		int startIdx =0;
		int endIdx = val.indexOf("APPLARIAT");
		StringBuffer sb = new StringBuffer("");
		while (endIdx>0) {
			sb.append(val.substring(startIdx, endIdx)+"/");
			startIdx=endIdx+9;
			endIdx = val.indexOf("APPLARIAT", startIdx);
		}
		if (sb.length()>0) {
			sb.append(val.substring(startIdx, val.length()));
			val = sb.toString();
		}
		byte[] decoded = Base64.getMimeDecoder().decode(val.trim());
	    return new String(decoded, StandardCharsets.UTF_8);
	}
	
	public static String encode(String val) {
		byte[] encoded = Base64.getMimeEncoder().encode(val.trim().getBytes());
		String b64 = new String(encoded, StandardCharsets.UTF_8);
		int startIdx=0;
		int endIdx = b64.indexOf("/");
		StringBuffer sb = new StringBuffer("");
		while (endIdx>=0) {
			sb.append(b64.substring(startIdx, endIdx)+"APPLARIAT");
			startIdx=endIdx+1;
			endIdx=b64.indexOf("/", startIdx);
		}
		if (sb.length()>0) {
			sb.append(b64.substring(startIdx, b64.length()));
			b64=sb.toString();
		}
	    return b64;
	}
}

