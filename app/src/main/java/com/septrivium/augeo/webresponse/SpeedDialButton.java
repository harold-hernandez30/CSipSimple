package com.septrivium.augeo.webresponse;

import android.widget.ImageButton;

/**
 * Created by harold on 7/22/2015.
 */
public class SpeedDialButton {

    private String label;
    private String icon;
    private String dial;

    private int dialButtonResId;
    private String number;

    public int getDialButtonResId() {
        return dialButtonResId;
    }

    public void setDialButtonResId(int dialButtonResId) {
        this.dialButtonResId = dialButtonResId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDial() {
        return dial;
    }

    public void setDial(String dial) {
        this.dial = dial;
    }

}
