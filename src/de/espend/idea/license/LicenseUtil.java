package de.espend.idea.license;

import com.google.gson.Gson;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.net.HttpConfigurable;
import com.jetbrains.ls.data.LicenseData;
import com.jetbrains.ls.requests.ValidateLicenseRequest;
import com.jetbrains.ls.responses.ValidateLicenseResponse;
import de.espend.idea.license.impl.ApplicationImplementation;
import de.espend.idea.license.request.LicenseRequest;
import de.espend.idea.license.request.RefreshRequest;
import org.apache.commons.net.util.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LicenseUtil {
    public static void validate(@NotNull String pluginId, @NotNull String code, @NotNull ApplicationImplementation application) {
        try {
            validatePlugin(pluginId, code, application);
        } catch (Exception e) {
            e.printStackTrace();
            application.invalidedLicense();
        }
    }

    private static void validatePlugin(@NotNull String pluginId, @NotNull String code, @NotNull ApplicationImplementation application) throws IOException {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(pluginId));
        if(plugin == null) {
            return;
        }

        String useLicenseUrl = "http://127.0.0.1:8000/refresh";

        //LicenseRequest request = new LicenseRequest(plugin.getPluginId().getIdString(), plugin.getVersion(), code, "admin", "12345");
        String tokenSaved = application.loadToken();
        if(tokenSaved == null) {
            System.out.println("foobar");
            return;
        }
        ValidateLicenseResponse
        ValidateLicenseRequest

        RefreshRequest refreshRequest = new RefreshRequest(pluginId, tokenSaved);

        HttpConfigurable httpConfigurable = ApplicationManager.getApplication().getComponent(HttpConfigurable.class);
        URLConnection urlConnection = null;
        urlConnection = httpConfigurable != null ?
            httpConfigurable.openConnection(useLicenseUrl) :
            (new URL(useLicenseUrl)).openConnection();

        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        urlConnection.connect();

        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(new Gson().toJson(refreshRequest).getBytes("UTF-8"));
        outputStream.flush();

        InputStream inputStream = urlConnection.getInputStream();

        String s = StreamUtil.readText(inputStream, "UTF-8");

        LicenseResponse licenseResponse = new Gson().fromJson(s, LicenseResponse.class);

        if(licenseResponse.isSuccess()) {
            String token = licenseResponse.getToken();
            if(token != null) {
                String tokenDecrypt = decrypt(licenseResponse.getToken(), "B8MYfA64XdMVZy7F");
                if(tokenDecrypt != null) {
                    Token tokenResult = new Gson().fromJson(tokenDecrypt, Token.class);

                    application.persistToken(token);
                    application.validatedLicense();
                    return;
                }
            }
        }

        System.out.println("invalid");
    }

    @Nullable
    public static String decrypt(@NotNull String input, @NotNull String key){
        byte[] bytes = Base64.decodeBase64(input);
        if(bytes.length < 17) {
            return null;
        }

        byte[] ivBytes = Arrays.copyOfRange(bytes, 0, 16);
        byte[] contentBytes = Arrays.copyOfRange(bytes, 16, bytes.length);


        try {
            Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");

            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
            IvParameterSpec iv = new IvParameterSpec(ivBytes,0, ciper.getBlockSize());

            ciper.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return new String(ciper.doFinal(contentBytes));
        } catch (
            NoSuchAlgorithmException |
                NoSuchPaddingException |
                UnsupportedEncodingException |
                InvalidAlgorithmParameterException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException ignored
            ) {
        }

        return null;
    }
}
