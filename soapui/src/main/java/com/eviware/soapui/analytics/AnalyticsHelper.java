/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.analytics;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.Version;
import com.eviware.soapui.analytics.providers.StatisticsCollectionConfirmationDialog;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.UISettings;
import com.smartbear.analytics.AnalyticsManager;
import com.smartbear.analytics.api.AnalyticsProviderFactory;
import com.smartbear.analytics.impl.GoogleAnalyticsProviderFactory;
import com.smartbear.analytics.impl.SoapUIOSMixpanelProviderFactory;

import javax.swing.JOptionPane;

public class AnalyticsHelper {
    private static boolean initialized = false;

    private static boolean isAnalyticsDisabled() {
        Settings settings = SoapUI.getSettings();
        boolean analyticsDisabled = settings.getBoolean(UISettings.DISABLE_ANALYTICS, false);
        if (analyticsDisabled) {
            return true;
        }
        Version optOutVersion = new Version(settings.getString(UISettings.ANALYTICS_OPT_OUT_VERSION, "0.0"));
        Version currentSoapUIVersion = new Version(SoapUI.SOAPUI_VERSION);
        if (!optOutVersion.getMajorVersion().equals(currentSoapUIVersion.getMajorVersion()) && SoapUI.usingGraphicalEnvironment()) {
            analyticsDisabled = StatisticsCollectionConfirmationDialog.showDialog() == JOptionPane.NO_OPTION;
            settings.setBoolean(UISettings.DISABLE_ANALYTICS, analyticsDisabled);
            settings.setString(UISettings.ANALYTICS_OPT_OUT_VERSION, currentSoapUIVersion.getMajorVersion());
        }
        return analyticsDisabled;
    }

    public static void initializeAnalytics() {
        if (initialized) {
            return;
        }
        initialized = true;
        UniqueUserIdentifier userIdentifier = UniqueUserIdentifier.getInstance();
        if (isAnalyticsDisabled()) {
            return;
        }
        AnalyticsManager manager = com.smartbear.analytics.Analytics.getAnalyticsManager();
        manager.setExecutorService(SoapUI.getThreadPool());
        manager.registerAnalyticsProviderFactory(new SoapUIOSMixpanelProviderFactory(SoapUIProductInfo.getInstance(), userIdentifier, AnalyticsProviderFactory.HandleType.MANDATORY));
        manager.registerAnalyticsProviderFactory(new GoogleAnalyticsProviderFactory(SoapUIProductInfo.getInstance()));
        manager.registerAnalyticsProviderFactory(new SoapUIOSMixpanelProviderFactory(SoapUIProductInfo.getInstance(), userIdentifier, AnalyticsProviderFactory.HandleType.USER_ALLOWED));
    }
}
