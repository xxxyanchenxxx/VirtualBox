package com.fun.vbox.client.hook.proxies.atm;

import com.fun.vbox.client.hook.proxies.am.MethodProxies.GetCallingActivity;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.GetCallingPackage;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.GetTasks;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.OverridePendingTransition;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.SetTaskDescription;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivities;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivity;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivityAndWait;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivityAsUser;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivityIntentSender;
import com.fun.vbox.client.hook.proxies.am.MethodProxies.StartActivityWithConfig;

public class MethodProxies {
    static class GetCallingActivityInAtm extends GetCallingActivity {
        GetCallingActivityInAtm() {
        }
    }

    static class GetCallingPackageInAtm extends GetCallingPackage {
        GetCallingPackageInAtm() {
        }
    }

    static class GetTasksInAtm extends GetTasks {
        GetTasksInAtm() {
        }
    }

    static class OverridePendingTransitionInAtm extends OverridePendingTransition {
        OverridePendingTransitionInAtm() {
        }
    }

    static class SetTaskDescriptionInAtm extends SetTaskDescription {
        SetTaskDescriptionInAtm() {
        }
    }

    static class StartActivitiesInAtm extends StartActivities {
        StartActivitiesInAtm() {
        }
    }

    static class StartActivityInAtm extends StartActivity {
        StartActivityInAtm() {
        }
    }

    static class StartActivityIntentSenderInAtm extends StartActivityIntentSender {
        StartActivityIntentSenderInAtm() {
        }
    }

    static class StartActivityAndWaitInAtm extends StartActivityAndWait {
        StartActivityAndWaitInAtm() {
        }
    }

    static class StartActivityAsUserInAtm extends StartActivityAsUser {
        StartActivityAsUserInAtm() {
        }
    }

    static class StartActivityWithConfigInAtm extends StartActivityWithConfig {
        StartActivityWithConfigInAtm() {
        }
    }
}
