package com.thoughtworks.calabash.android;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {
    private final List<String> bonusInformation = new ArrayList<String>();
    private final String message;
    private final boolean success;

    public ActionResult(Object[] bonusInformationObjectList, String message, boolean success) {
        for (Object bonusInfo : bonusInformationObjectList) {
            bonusInformation.add(bonusInfo.toString());
        }
        this.message = message;
        this.success = success;
    }

    public List<String> getBonusInformation() {
        return bonusInformation;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
