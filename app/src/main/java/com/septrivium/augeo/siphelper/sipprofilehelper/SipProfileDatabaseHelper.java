package com.septrivium.augeo.siphelper.sipprofilehelper;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.models.Filter;
import com.csipsimple.wizards.WizardIface;
import com.csipsimple.wizards.WizardUtils;

import java.util.List;

/**
 * Created by harold on 7/16/2015.
 */
public class SipProfileDatabaseHelper {

    public static void createProfileAndRegister(Context context, SipProfile account) {
        context.getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
        Uri uri = context.getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());

        String wizardId = "SIP2SIP";

        //					        onClickAddAccount();

        WizardUtils.WizardInfo wizardInfo = WizardUtils.getWizardClass(wizardId);
        WizardIface wizard = null;

        try {
            wizard = (WizardIface) wizardInfo.classObject.newInstance();
        } catch (Exception e) {
        }

        // After insert, add filters for this wizard
        account.id = ContentUris.parseId(uri);
        List<Filter> filters = wizard.getDefaultFilters(account);
        if (filters != null) {
            for (Filter filter : filters) {
                // Ensure the correct id if not done by the wizard
                filter.account = (int) account.id;
                context.getContentResolver().insert(SipManager.FILTER_URI, filter.getDbContentValues());
            }
        }
    }
}
