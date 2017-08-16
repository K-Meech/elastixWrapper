package registrationTools;

import ij.IJ;
import ij.ImagePlus;
import registrationTools.logging.IJLazySwingLogger;
import registrationTools.logging.Logger;
import registrationTools.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by tischi on 30/04/17.
 */
public class RegistrationToolsGUI extends JFrame implements ActionListener, FocusListener, ItemListener {

    public final static String IMAGEPLUS = "from ImageJ";
    public final static String ELASTIX = "Elastix";

    JTextField tfElastixFolder = new JTextField(40);
    JTextField tfTmpFolder = new JTextField(40);
    JTextField tfNumIterations = new JTextField(12);
    JTextField tfNumSpatialSamples = new JTextField(12);
    JTextField tfResolutionPyramid = new JTextField(12);
    JTextField tfBSplineGridSpacing = new JTextField(12);
    JTextField tfNumWorkers = new JTextField(3);
    JTextField tfReference = new JTextField(3);
    JTextField tfRegRange = new JTextField(3);
    JTextField tfZRange = new JTextField(4);
    JTextField tfBackground = new JTextField(4);
    JComboBox comboTransform = new JComboBox(RegistrationSettings.Type.values());
    JCheckBox cbRecursive = new JCheckBox();

    JButton btRunElastix = new JButton("Run Registration");

    RegistrationSettings settings = new RegistrationSettings();

    Logger logger = new IJLazySwingLogger();

    /**
     *
     * input: current image, folder
     * output: new image, folder
     *
     * method: Elastix
     *
     * Elastix settings: ...
     *
     * actions: [Run registration]
     *
     */
    public RegistrationToolsGUI()
    {

    }

    public void showDialog()
    {
        JTabbedPane jtp = new JTabbedPane();
        ArrayList<JPanel> panels = new ArrayList<>();
        ArrayList<JPanel> tabs = new ArrayList<>();

        // suggest settings based on current image
        //
        ImagePlus imp = IJ.getImage();
        if ( imp != null )
        {
            double dimRatio = (imp.getWidth() + imp.getHeight()) / imp.getNSlices();
            int zBinning = (int)Math.ceil(10/dimRatio);
            settings.resolutionPyramid = imp.getNSlices() > 1 ? "10 10 "+zBinning+"; 2 2 1" : "10 10; 2 2";
            int bsgsX = (int) Math.ceil( imp.getWidth() / 5 );
            int bsgsY = (int) Math.ceil( imp.getHeight() / 5) ;
            int bsgsZ = (int) Math.ceil( imp.getNSlices() / 5) ;
            settings.bSplineGridSpacing = imp.getNSlices() > 1 ?
                    ""+bsgsX+" "+bsgsY+" "+bsgsZ : ""+bsgsX+" "+bsgsY;

            settings.bitDepth = imp.getBitDepth();

            settings.regRange[0] = 1;
            settings.regRange[1] = imp.getNFrames();

            settings.zRange[0] = 1;
            settings.zRange[1] = imp.getNSlices();

        }

        // suggest some settings based on current OS
        //
        String os = System.getProperty("os.name");
        logger.info("Operating system: "+os);

        if ( os.toLowerCase().contains("mac") )
        {
            logger.info("Choosing Mac OS");
            settings.os = "Mac";
            settings.folderElastix = "/Users/tischi/Downloads/elastix_macosx64_v4.8/";
            settings.folderTmp = "/Users/"+System.getProperty("user.name")+"/Desktop/tmp/";
        }
        if (os.toLowerCase().contains("windows"))
        {
            logger.info("Choosing Windows OS");
            settings.os = "Windows";
            settings.folderElastix = "C:\\Program Files\\elastix_v4.8\\";
            settings.folderTmp = "C:\\Users\\"+System.getProperty("user.name")+"\\Desktop\\tmp\\";
        }
        else
        {
            logger.error("Your operation system is currently not supported: " + os);
            return;
        }

        addTabPanel(tabs);
        addHeader(panels, tabs, "ELASTIX CONFIGURATION");
        addTextField(panels, tabs, tfElastixFolder, "Installation folder", "" + settings.folderElastix);
        addTextField(panels, tabs, tfTmpFolder, "Temp folder", "" + settings.folderTmp);

        addHeader(panels, tabs, "PARAMETERS");
        addComboBox(panels, tabs, comboTransform, "Transform");
        addTextField(panels, tabs, tfNumIterations, "Iterations", "" + settings.iterations);
        addTextField(panels, tabs, tfNumSpatialSamples, "Spatial samples", ""+settings.spatialSamples);
        addTextField(panels, tabs, tfResolutionPyramid, "Resolution pyramid", settings.resolutionPyramid);
        addTextField(panels, tabs, tfBSplineGridSpacing, "BSpline grid spacing", settings.bSplineGridSpacing);
        addTextField(panels, tabs, tfNumWorkers, "Threads", "" + settings.workers);
        addTextField(panels, tabs, tfReference, "Reference frame", "" + settings.reference);
        addTextField(panels, tabs, tfRegRange, "Temporal range (first frame, last frame)", "" + settings.regRange[0]+","+settings.regRange[1]);
        addTextField(panels, tabs, tfZRange, "Axial range (first z, last z)", "" + settings.zRange[0]+","+settings.zRange[1]);
        addTextField(panels, tabs, tfBackground, "Image background value", "" + settings.background);

        addCheckBox(panels, tabs, cbRecursive, "Recursive registration", settings.recursive);

        addHeader(panels, tabs, "RUN");
        addButton(panels, tabs, btRunElastix);
        jtp.add("Elastix", tabs.get(tabs.size() - 1));

        // Show
        //
        setTitle("Registration Tools");
        add(jtp);
        setVisible(true);
        pack();
    }


    public static void addTabPanel(ArrayList<JPanel> tabs)
    {
        tabs.add(new JPanel());
        tabs.get(tabs.size()-1).setLayout(new BoxLayout(tabs.get(tabs.size()-1), BoxLayout.PAGE_AXIS));
    }


    public static void addHeader(ArrayList<JPanel> panels, ArrayList<JPanel> tabs, String label)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(panels.size()-1).add(new JLabel(label));
        tabs.get(tabs.size()-1).add(panels.get(panels.size() - 1));
    }

    public void addTextField(ArrayList<JPanel> panels, ArrayList<JPanel> tabs, JTextField textField,
                             String label, String defaultValue)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        textField.setActionCommand(label);
        textField.addActionListener(this);
        textField.addFocusListener(this);
        textField.setText(defaultValue);
        panels.get(panels.size()-1).add(new JLabel(label));
        panels.get(panels.size()-1).add(textField);
        tabs.get(tabs.size()-1).add(panels.get(panels.size()-1));
    }

    public void addCheckBox(ArrayList<JPanel> panels, ArrayList<JPanel> tabs, JCheckBox checkBox,
                             String label, Boolean defaultValue)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        checkBox.setSelected(defaultValue);
        panels.get(panels.size()-1).add(new JLabel(label));
        panels.get(panels.size()-1).add(checkBox);
        tabs.get(tabs.size()-1).add(panels.get(panels.size()-1));
    }


    public void addButton(ArrayList<JPanel> panels, ArrayList<JPanel> tabs, JButton button)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        button.setActionCommand(button.getText());
        button.addActionListener(this);
        panels.get(panels.size()-1).add(button);
        tabs.get(tabs.size()-1).add(panels.get(panels.size()-1));
    }

    public void addComboBox(ArrayList<JPanel> panels, ArrayList<JPanel> tabs, JComboBox comboBox, String comboBoxLabel)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(panels.size()-1).add(new JLabel(comboBoxLabel));
        panels.get(panels.size()-1).add(comboBox);
        tabs.get(tabs.size() - 1).add(panels.get(panels.size()-1));
    }

    public void actionPerformed(ActionEvent e)
    {
        if ( e.getActionCommand().equals(btRunElastix.getText()) )
        {

            settings.method = RegistrationToolsGUI.ELASTIX;
            settings.type = (RegistrationSettings.Type) comboTransform.getSelectedItem();
            settings.recursive = cbRecursive.isSelected();
            settings.iterations = Integer.parseInt(tfNumIterations.getText());
            settings.spatialSamples = tfNumSpatialSamples.getText();
            settings.workers = Integer.parseInt(tfNumWorkers.getText());
            settings.resolutionPyramid = tfResolutionPyramid.getText();
            settings.regRange = Utils.delimitedStringToIntegerArray(tfRegRange.getText(), ",");
            settings.zRange = Utils.delimitedStringToIntegerArray(tfZRange.getText(), ",");
            settings.background = Double.parseDouble( tfBackground.getText() );
            settings.reference = Integer.parseInt( tfReference.getText() );
            settings.folderTmp = tfTmpFolder.getText();
            settings.folderElastix = tfElastixFolder.getText();
            settings.roi = IJ.getImage().getRoi();
            settings.bSplineGridSpacing = tfBSplineGridSpacing.getText();
            IJ.getImage().deleteRoi(); // otherwise the duplicators later only duplicate the roi

            String inputImages = RegistrationToolsGUI.IMAGEPLUS;
            String outputImages = RegistrationToolsGUI.IMAGEPLUS;

            RegistrationTools registrationTools = new RegistrationTools(inputImages,
                    outputImages, settings);
            registrationTools.run();
        }
    }

    @Override
    public void focusGained(FocusEvent e)
    {

    }

    @Override
    public void focusLost(FocusEvent e)
    {
        JTextField tf = (JTextField) e.getSource();
        if ( tf != null )
        {
            tf.postActionEvent();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {

    }
}
