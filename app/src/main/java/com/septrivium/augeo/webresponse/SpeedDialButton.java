package com.septrivium.augeo.webresponse;

import android.widget.ImageButton;

/**
 * Created by harold on 7/22/2015.
 */
public class SpeedDialButton {

    private String label;
    private String icon;
    private String dial;

    private boolean hasCombinedIcons;

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

    public boolean hasCombinedIcons() {
        return hasCombinedIcons;
    }

    public void setHasCombinedIcons(boolean hasCombinedIcons) {
        this.hasCombinedIcons = hasCombinedIcons;
    }

    public String getCombinedIconKey() {
        return new StringBuilder().append(icon).append("_combined").toString();
    }

    @Override
    public String toString() {
        return "SpeedDialButton{" +
                "label='" + label + '\'' +
                ", icon='" + icon + '\'' +
                ", dial='" + dial + '\'' +
                ", hasCombinedIcons=" + hasCombinedIcons +
                ", dialButtonResId=" + dialButtonResId +
                ", number='" + number + '\'' +
                '}';
    }
}
