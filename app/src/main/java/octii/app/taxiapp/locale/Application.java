/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 17.08.2021, 22:43                       *
 ******************************************************************************/

package octii.app.taxiapp.locale;

import android.content.Context;

public class Application extends android.app.Application {

    private static Application applicationInstance;

    public static synchronized Application getInstance() {
        return applicationInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationInstance = this;
    }

    public void initAppLanguage(Context context) {
        LocaleUtils.initialize(context, LocaleUtils.getSelectedLanguageId());
    }


}
