package mm2python;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bryant.chhun
 */

import mm2python.UI.pythonBridgeUI_dialog;
import mmcorej.CMMCore;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

@Plugin(type = MenuPlugin.class)
public class mm2PythonPlugin implements MenuPlugin, SciJavaPlugin{
    private static final String menuName = "mm2Python_v0.0.1";
    private static final String tooltipDescription = "Establishes a Py4J gateway for Python control of Micro-Manager and access to data";
    private static final String version =  "0.0.1";
    private static final String copyright = "CZ Biohub";

    // Provides access to the Micro-Manager Core API (for direct hardware
    // control)
    private CMMCore mmc_;

    // Provides access to the Micro-Manager Java API (for GUI control and high-
    // level functions).
    private Studio mm_;

    private pythonBridgeUI_dialog myFrame_;


    @Override
    public String getSubMenu() {
        return "mm2Python";
    }

    @Override
    public void onPluginSelected() {
        if (myFrame_ == null) {
            try {
//                LocalStudio ls = new LocalStudio(mm_);
//                LocalCore lc = new LocalCore(mmc_);
//                myFrame_ = new pythonBridgeUI_dialog(ls.getStudio(), lc.getCore());
                myFrame_ = new pythonBridgeUI_dialog(mm_, mmc_);
                mm_.events().registerForEvents(myFrame_);
            } catch (Exception e) {
                mm_.logs().showError(e);
            }
        }
        myFrame_.pack();
        myFrame_.setVisible(true);
    }

    @Override
    public void setContext(Studio app) {
        mm_ = app;
        mmc_ = app.getCMMCore();
    }

    @Override
    public String getName() {
        return menuName;
    }

    @Override
    public String getHelpText() {
        return tooltipDescription;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getCopyright() {
        return copyright;
    }

}