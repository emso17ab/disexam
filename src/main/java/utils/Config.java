package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Config {

  private static String SSH_TUNNEL_HOST;
  private static int SSH_TUNNEL_PORT;
  private static String SSH_TUNNEL_USERNAME;
  private static String SSH_TUNNEL_PASSWORD;
  private static int SSH_TUNNEL_REMOTEPORT;
  private static String DATABASE_HOST;
  private static int DATABASE_PORT;
  private static String DATABASE_USERNAME;
  private static String DATABASE_PASSWORD;
  private static String DATABASE_NAME;
  private static boolean ENCRYPTION;
  private static String ENCRYPTION_KEY;
  private static String SOLR_HOST;
  private static int SOLR_PORT;
  private static String SOLR_PATH;
  private static String SOLR_CORE;
  private static long CACHE_TTL;
  private static boolean CACHE_FORCE_UPDATE;
  private static long TOKEN_TTL;
  private static String AUTHENTICATOR_KEY;

  public static String getSshTunnelHost() {
    return SSH_TUNNEL_HOST;
  }

  public static int getSshTunnelPort() {
    return SSH_TUNNEL_PORT;
  }

  public static String getSshTunnelUsername() {
    return SSH_TUNNEL_USERNAME;
  }

  public static String getSshTunnelPassword() {
    return SSH_TUNNEL_PASSWORD;
  }

  public static int getSshTunnelRemoteport() {
    return SSH_TUNNEL_REMOTEPORT;
  }

  public static long getCacheTtl() {
    return CACHE_TTL;
  }

  public static boolean getCacheForceUpdate() {
    return CACHE_FORCE_UPDATE;
  }

  public static String getDatabaseHost() {
    return DATABASE_HOST;
  }

  public static int getDatabasePort() {
    return DATABASE_PORT;
  }

  public static String getDatabaseUsername() {
    return DATABASE_USERNAME;
  }

  public static String getDatabasePassword() {
    return DATABASE_PASSWORD;
  }

  public static String getDatabaseName() {
    return DATABASE_NAME;
  }

  public static Boolean getEncryption() {
    return ENCRYPTION;
  }

  public static char[] getEncryptionKey() {
    return ENCRYPTION_KEY.toCharArray();
  }

  public static String getSolrHost() {
    return SOLR_HOST;
  }

  public static int getSolrPort() {
    return SOLR_PORT;
  }

  public static String getSolrPath() {
    return SOLR_PATH;
  }

  public static String getSolrCore() {
    return SOLR_CORE;
  }

  public static long getTokenTtl() {
    return TOKEN_TTL;
  }

  public static String getAuthenticatorKey() {
    return AUTHENTICATOR_KEY;
  }

  public static void initializeConfig() throws IOException {

    // Init variables to parse JSON
    JsonObject json;
    JsonParser parser = new JsonParser();

    // Read File and store input
    InputStream input = Config.class.getResourceAsStream("/config.json");
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

    // Go through the lines one by one
    StringBuffer stringBuffer = new StringBuffer();
    String str;

    // Read file one line at a time
    while ((str = reader.readLine()) != null) {
      stringBuffer.append(str);
    }

    // Konverterer json til variabler ved at typecaste til JsonObject
    json = (JsonObject) parser.parse(stringBuffer.toString());

    // Hiv teksten ud og sæt klassens variable til senere brug
    SSH_TUNNEL_HOST = json.get("SSH_TUNNEL_HOST").toString().replace("\"","");
    SSH_TUNNEL_PORT = Integer.parseInt(json.get("SSH_TUNNEL_PORT").toString().replace("\"",""));
    SSH_TUNNEL_USERNAME = json.get("SSH_TUNNEL_USERNAME").toString().replace("\"","");
    SSH_TUNNEL_PASSWORD = json.get("SSH_TUNNEL_PASSWORD").toString().replace("\"","");
    SSH_TUNNEL_REMOTEPORT = Integer.parseInt(json.get("SSH_TUNNEL_REMOTEPORT").toString().replace("\"",""));
    DATABASE_HOST = json.get("DATABASE_HOST").toString().replace("\"", "");
    DATABASE_PORT = Integer.parseInt(json.get("DATABASE_PORT").toString().replace("\"", ""));
    DATABASE_USERNAME = json.get("DATABASE_USERNAME").toString().replace("\"", "");
    DATABASE_PASSWORD = json.get("DATABASE_PASSWORD").toString().replace("\"", "");
    DATABASE_NAME = json.get("DATABASE_NAME").toString().replace("\"", "");
    ENCRYPTION = json.get("ENCRYPTION").getAsBoolean();
    ENCRYPTION_KEY = json.get("ENCRYPTION_KEY").getAsString();
    SOLR_HOST = json.get("SOLR_HOST").toString().replace("\"", "");
    SOLR_PORT = Integer.parseInt(json.get("SOLR_PORT").toString().replace("\"", ""));
    SOLR_PATH = json.get("SOLR_PATH").toString().replace("\"", "");
    SOLR_CORE = json.get("SOLR_CORE").toString().replace("\"", "");
    CACHE_TTL = json.get("CACHE_TTL").getAsLong();
    CACHE_FORCE_UPDATE = json.get("CACHE_FORCE_UPDATE").getAsBoolean();
    TOKEN_TTL = json.get("TOKEN_TTL").getAsLong();
    AUTHENTICATOR_KEY = json.get("AUTHENTICATOR_KEY").toString().replace("\"","");
  }
}