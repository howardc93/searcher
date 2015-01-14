import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//TODO
//-allow delay between tab opens
//-auto go to bottom of textarea

public class SearcherV2 {
        private JFrame frame;
        private JTextArea textarea;
        private JTextField textfield;
        private JPanel catpanel;
        private JComboBox opselect;
        private Preferences prefs;
        private HashMap<String, ArrayList<String>> dataMap;
        private HashMap<String, String> URLmap;
        private HashMap<String, JCheckBox> checkBoxMap;

        private void createURLMap() {
                URLmap = new HashMap<String, String>();
                URLmap.put("Google", "http://www.google.com/search?q=");
                URLmap.put("Wikipedia", "http://en.wikipedia.org/w/index.php?search=");
                URLmap.put("Bing", "http://www.bing.com/search?q=");
        }

        public void runCommand() {
                setPreference();
                try {
                        for (String key : dataMap.keySet()) {
                                String URL = URLmap.get(opselect.getSelectedItem().toString());
                                // check if corresponding checkbox is selected
                                if (checkBoxMap.get(key).isSelected()) {
                                        // loop through values in key
                                        for (int i = 0; i < dataMap.get(key).size(); i++) {
                                                // append chosen URL to item
                                                String item = URL + dataMap.get(key).get(i);
                                                // replace spaces to make valid URI
                                                URI uri = new URI(item.replace(" ", "%20"));
                                                java.awt.Desktop.getDesktop().browse(uri);
                                        }
                                }
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        textarea.append(e.getMessage());
                }
        }

        public void setPreference() {
                if (!textfield.getText().isEmpty()) {
                        prefs.put("savedPath", textfield.getText());
                }
                prefs.put("savedOp", opselect.getSelectedItem().toString());

        }

        public void load() {
                generateMap(textfield.getText());
                updateCats();
                System.out.println(dataMap);
        }

        private void generateMap(String input) {
                BufferedReader br;
                try {
                        if (input.startsWith("http")) {
                                URL url = new URL(input);
                                InputStream is = url.openConnection().getInputStream();
                                br = new BufferedReader(new InputStreamReader(is));
                        } else {
                                File chosenFile = new File(input);
                                FileReader reader = new FileReader(chosenFile);
                                br = new BufferedReader(reader);
                        }
                        dataMap = new HashMap<String, ArrayList<String>>();
                        String line = "";
                        String currCat = "";
                        int catCount = 0;
                        int itemCount = 0;
                        while ((line = br.readLine()) != null) {
                                line = line.trim();
                                if (!line.isEmpty()) {
                                        if (line.startsWith("CAT:")) {
                                                currCat = line.substring(4).trim();
                                                // add new cat to map
                                                dataMap.put(currCat, new ArrayList<String>());
                                                catCount++;
                                        } else {
                                                // add item to latest cat
                                                dataMap.get(currCat).add(line);
                                                itemCount++;
                                        }
                                }
                        }
                        br.close();
                        textarea.append(catCount + " categories loaded\n");
                        textarea.append(itemCount + " items loaded\n");

                } catch (MalformedURLException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }

        }

        /**
         * Updates the checkboxes of categories
         */
        private void updateCats() {
                checkBoxMap = new HashMap<String, JCheckBox>();
                catpanel.removeAll();
                for (String key : dataMap.keySet()) {
                        checkBoxMap.put(key, new JCheckBox(key, true));
                        catpanel.add(checkBoxMap.get(key));
                }
                frame.validate();
        }

        /**
         * Makes the GUI
         */
        private void createGUI() {
                // This will define a node in which the preferences can be stored
                prefs = Preferences.userRoot().node(this.getClass().getName());

                frame = new JFrame("Rapid Searcher");
                frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
                JPanel labelPanel = new JPanel();
                labelPanel.add(new JLabel("Enter file path or web address"));

                JPanel textFieldPanel = new JPanel();
                JButton submitbutton = new JButton("Search!");
                submitbutton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                runCommand();
                        }
                });
                JButton openbutton = new JButton("Open");
                openbutton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                load();
                        }
                });
                textfield = new JTextField(prefs.get("savedPath", ""), 30);
                textfield.setEditable(true);
                textFieldPanel.add(textfield);
                textFieldPanel.add(openbutton);
                textFieldPanel.add(submitbutton);

                JPanel selectpanel = new JPanel();
                selectpanel.add(new JLabel("Select an operation"));
                opselect = new JComboBox();
                for (String key : URLmap.keySet()) {
                        opselect.addItem(key);
                }
                opselect.setSelectedItem(prefs.get("savedOp", "Google"));
                selectpanel.add(opselect);

                catpanel = new JPanel();
                catpanel.setLayout(new BoxLayout(catpanel, 0));

                JPanel panel2 = new JPanel();
                textarea = new JTextArea(10, 30);
                textarea.setEditable(false);
                JScrollPane scrollpane = new JScrollPane(textarea);
                scrollpane
                                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                panel2.add(scrollpane);

                JPanel bottompanel = new JPanel();
                bottompanel.add(new JLabel("Written by Howard Chung"));

                frame.add(labelPanel);
                frame.add(textFieldPanel);
                frame.add(selectpanel);
                frame.add(catpanel);
                frame.add(panel2);
                frame.add(bottompanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.pack();
                frame.setVisible(true);

        }

        public void initialize() {
                createURLMap();
                createGUI();
        }

        public static void main(String[] args) {
                SearcherV2 run = new SearcherV2();
                run.initialize();
        }

}