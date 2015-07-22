package com.septrivium.augeo.vpnhelper;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import com.csipsimple.R;
import com.septrivium.augeo.webresponse.DeviceProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashSet;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;

/**
 * Created by harold on 7/9/2015.
 */
public class ConfigConverter {

    private static VpnProfile mResult;
    private Context mContext;
    private String mEmbeddedPwFile;
    private String mCrlFileName;
    private AbstractMap mPathsegments;

    public ConfigConverter(Context context) {

        mContext = context;
    }


    public void saveProfile(VpnProfile mResult, Context context) {
        ProfileManager vpl = ProfileManager.getInstance(context);

//        if (!TextUtils.isEmpty(mEmbeddedPwFile))
//            ConfigParser.useEmbbedUserAuth(mResult, mEmbeddedPwFile);
//
//        if (!TextUtils.isEmpty(mCrlFileName)) {
//            // TODO: COnvert this to a real config option that is parsed
//            ConfigParser.removeCRLCustomOption(mResult);
//            mResult.mCustomConfigOptions += "crl-verify " + mCrlFileName;
//        }

        vpl.addProfile(mResult);
        vpl.saveProfile(context, mResult);
        vpl.saveProfileList(context);
    }

    public VpnProfile doImportFromAsset(String assetName, DeviceProfile deviceProfile) throws IOException, ConfigParser.ConfigParseError {
        ConfigParser cp = new ConfigParser();

        InputStreamReader isr = new InputStreamReader(mContext.getResources().getAssets().open(assetName));

        cp.parseConfig(isr);
        mResult = cp.convertProfile();
        embedFiles(cp);
        mResult.mName = "auGeo_android";
        mResult.mUsername = deviceProfile.getVpnUsername();
        mResult.mPassword = deviceProfile.getVpnPassword();
        mResult.mServerName = deviceProfile.getVpnServerDomain();
        mResult.mServerPort = deviceProfile.getVpnServerPort();
        saveProfile(mResult, mContext);

        return mResult;


    }


    void embedFiles(ConfigParser cp) {
        // This where I would like to have a c++ style
        // void embedFile(std::string & option)


        mResult.mCaFilename = embedFile(mResult.mCaFilename, Utils.FileType.CA_CERTIFICATE, false);
        mResult.mClientCertFilename = embedFile(mResult.mClientCertFilename, Utils.FileType.CLIENT_CERTIFICATE, false);
        mResult.mClientKeyFilename = embedFile(mResult.mClientKeyFilename, Utils.FileType.KEYFILE, false);
        mResult.mTLSAuthFilename = embedFile(mResult.mTLSAuthFilename, Utils.FileType.TLS_AUTH_FILE, false);
        mResult.mPKCS12Filename = embedFile(mResult.mPKCS12Filename, Utils.FileType.PKCS12, false);
        mEmbeddedPwFile = cp.getAuthUserPassFile();
        mEmbeddedPwFile = embedFile(cp.getAuthUserPassFile(), Utils.FileType.USERPW_FILE, false);
        mCrlFileName = embedFile(cp.getCrlVerifyFile(), Utils.FileType.CRL_FILE, true);
    }

    private String embedFile(String filename, Utils.FileType type, boolean onlyFindFile) {
        if (filename == null)
            return null;

        // Already embedded, nothing to do
        if (VpnProfile.isEmbedded(filename))
            return filename;

        File possibleFile = findFile(filename, type);
        if (possibleFile == null)
            return filename;
        else if (onlyFindFile)
            return possibleFile.getAbsolutePath();
        else
            return readFileContent(possibleFile, type == Utils.FileType.PKCS12);

    }

    public File findFile(String filename, Utils.FileType fileType) {
        File foundfile = findFileRaw(filename);

        if (foundfile == null && filename != null && !filename.equals("")) {
            if (fileType != Utils.FileType.CRL_FILE)
                addFileSelectDialog(fileType);
        }


        return foundfile;
    }

    private void addFileSelectDialog(Utils.FileType type) {
        int titleRes = 0;
        String value = null;
        switch (type) {
            case KEYFILE:
                titleRes = R.string.client_key_title;
                if (mResult != null)
                    value = mResult.mClientKeyFilename;
                break;
            case CLIENT_CERTIFICATE:
                titleRes = R.string.client_certificate_title;
                if (mResult != null)
                    value = mResult.mClientCertFilename;
                break;
            case CA_CERTIFICATE:
                titleRes = R.string.ca_title;
                if (mResult != null)
                    value = mResult.mCaFilename;
                break;
            case TLS_AUTH_FILE:
                titleRes = R.string.tls_auth_file;
                if (mResult != null)
                    value = mResult.mTLSAuthFilename;
                break;
            case PKCS12:
                titleRes = R.string.client_pkcs12_title;
                if (mResult != null)
                    value = mResult.mPKCS12Filename;
                break;

            case USERPW_FILE:
                titleRes = R.string.userpw_file;
                value = mEmbeddedPwFile;
                break;

            case CRL_FILE:
                titleRes = R.string.crl_file;
                value = mCrlFileName;
                break;
        }

        boolean isCert = type == Utils.FileType.CA_CERTIFICATE || type == Utils.FileType.CLIENT_CERTIFICATE;
//        FileSelectLayout fl = new FileSelectLayout(this, getString(titleRes), isCert, false);
//        fileSelectMap.put(type, fl);
//        fl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//        ((LinearLayout) findViewById(R.id.config_convert_root)).addView(fl, 2);
//        findViewById(R.id.files_missing_hint).setVisibility(View.VISIBLE);
//        fl.setData(value, this);
//        int i = getFileLayoutOffset(type);
//        fl.setCaller(this, i, type);

    }

    private File findFileRaw(String filename) {
        if (filename == null || filename.equals(""))
            return null;

        // Try diffent path relative to /mnt/sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        File root = new File("/");

        HashSet<File> dirlist = new HashSet<File>();

        for (int i = mPathsegments.size() - 1; i >= 0; i--) {
            String path = "";
            for (int j = 0; j <= i; j++) {
                path += "/" + mPathsegments.get(j);
            }
            // Do a little hackish dance for the Android File Importer
            // /document/primary:ovpn/openvpn-imt.conf


            if (path.indexOf(':') != -1 && path.lastIndexOf('/') > path.indexOf(':')) {
                String possibleDir = path.substring(path.indexOf(':') + 1, path.length());
                possibleDir = possibleDir.substring(0, possibleDir.lastIndexOf('/'));


                dirlist.add(new File(sdcard, possibleDir));

            }
            dirlist.add(new File(path));


        }
        dirlist.add(sdcard);
        dirlist.add(root);


        String[] fileparts = filename.split("/");
        for (File rootdir : dirlist) {
            String suffix = "";
            for (int i = fileparts.length - 1; i >= 0; i--) {
                if (i == fileparts.length - 1)
                    suffix = fileparts[i];
                else
                    suffix = fileparts[i] + "/" + suffix;

                File possibleFile = new File(rootdir, suffix);
                if (!possibleFile.canRead())
                    continue;

                // read the file inline
                return possibleFile;

            }
        }
        return null;
    }

    String readFileContent(File possibleFile, boolean base64encode) {
        byte[] filedata;
        try {
            filedata = readBytesFromFile(possibleFile);
        } catch (IOException e) {
            return null;
        }

        String data;
        if (base64encode) {
            data = Base64.encodeToString(filedata, Base64.DEFAULT);
        } else {
            data = new String(filedata);

        }

        return VpnProfile.DISPLAYNAME_TAG + possibleFile.getName() + VpnProfile.INLINE_TAG + data;

    }


    private byte[] readBytesFromFile(File file) throws IOException {
        InputStream input = new FileInputStream(file);

        long len = file.length();
        if (len > VpnProfile.MAX_EMBED_FILE_SIZE)
            throw new IOException("File size of file to import too large.");

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) len];

        // Read in the bytes
        int offset = 0;
        int bytesRead;
        while (offset < bytes.length
                && (bytesRead = input.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += bytesRead;
        }

        input.close();
        return bytes;
    }

}
