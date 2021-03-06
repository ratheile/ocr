package ch.zhaw.ocr.gui.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import ch.zhaw.ocr.dictionary.Dictionary;
import ch.zhaw.ocr.gui.MainGui;
import ch.zhaw.ocr.gui.helper.ReadOnlyTableModel;
import ch.zhaw.ocr.gui.helper.SpringUtilities;

/**
 * DictionaryTab offerrs an UI for an overview about all words in the dictionary and
 * also offers the opportunity to change words
 * @author Corinne Zeugin, Priscilla Schneider, Adrian Schmid
 *
 */
public class DictionaryTab {	
	private final static int SHOWENTRIES = 500;

	private JPanel panel;
	private JPanel areaPanel;
	
	private JTable wordsText;
	private ReadOnlyTableModel model;
	
	private JButton nextButton;
	
	private Set<String> strings;
	List<String[]> rows;
	Vector<String> colTitles;

	private Dictionary dic;
	private List<String> charStrings;
	private int indexOfChar = -1;
	
	private JTextField correctField;
	private JTextField resultField;
	
	/**
	 * Constructor
	 * @param gui main gui
	 */
    public DictionaryTab(MainGui gui) {
        this.dic = gui.getOcr().getDictionary();

    	nextButton = new JButton("Show next entries");
    	charStrings = new ArrayList<String>(Arrays.asList("a", "b", "c", "d",
				"e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
				"q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
        createPanel();
    }
    
    /**
     * this method creates the dictionary-tab-main-panel with all elements (textarea, JTree, Button)
     */
    public void createPanel() {
    	
    	panel = new JPanel(new BorderLayout());
    	panel.add(getAbcPanel(), BorderLayout.NORTH);
        areaPanel = new JPanel(new SpringLayout());
        
        model = new ReadOnlyTableModel(new String[]{"Wort", "Häufigkeit"}, 0);
        
        wordsText = new JTable(model);
        //wordsText.setModel(model);
        
        JScrollPane scrollArea = new JScrollPane (wordsText);
        areaPanel.add(scrollArea);
        
    	
    	nextButton.addActionListener(new NextButtonListener());
    	
    	JPanel centerPanel = new JPanel(new BorderLayout());
    	
    	centerPanel.add(nextButton, BorderLayout.SOUTH);
        SpringUtilities.makeCompactGrid(areaPanel, 1, 1, 10, 10, 10, 20);
        centerPanel.add(areaPanel, BorderLayout.CENTER);
        
    	panel.add(centerPanel, BorderLayout.CENTER);
    	
    	panel.add(createLookupPanel(), BorderLayout.SOUTH);
    }
    
    private void clearTableEntries(){
    	while(model.getRowCount() > 0){
    		model.removeRow(0);
    	}
    }
    
    private List<String[]> getDicEntries(String charString) {
    	rows = new LinkedList<String[]>();
    	
    	indexOfChar = 0;
    	
    	strings = new TreeSet<String>();
	    for (String dicEntry : dic.getDictionary().keySet()) {
	    	if (dicEntry.startsWith(charString)) {
	    		strings.add(dicEntry);
	    	}
	    }
    	
    	
    	if (SHOWENTRIES <= strings.size()) {
    		indexOfChar = SHOWENTRIES;
    	}
    	
    	int i = 0;
    	for (String s : strings) {
    		String[] row = new String[2];
    		row[0] = s;
    		row[1] = dic.getDictionary().get(s) + "";
    		
    		rows.add(row);
    		
    		i ++;
    		if (i >= SHOWENTRIES) {
    			break;
    		}
    	}
    	
    	if (i >= SHOWENTRIES) {
    		nextButton.setVisible(true);
    	} else {
    		nextButton.setVisible(false);
    	}
    	
    	
    	return rows;
    }
    
    private List<String[]> getMoreDicEntries() {
    	
    	int i = 0;
    	boolean hasMore = false;
    	for (String s : strings) {
    		if (i > indexOfChar) {
    			String[] row = new String[2];
        		row[0] = s;
        		row[1] = dic.getDictionary().get(s) + "";
        		
        		rows.add(row);
    		}
    		i ++;
    		
    		if (i >= indexOfChar + SHOWENTRIES) {
    			indexOfChar += SHOWENTRIES;
    			hasMore = true;
    			break;
    		}
    		
    	}
    	
    	if (!hasMore) {
    		nextButton.setVisible(false);
    	}
    	
    	return rows;
    }
    
    private JPanel getAbcPanel() {
    	JPanel abcPanel = new JPanel();
    	
    	List<JButton> buttonList = createAbcButtons();
    	int i = 0;
    	for (JButton button : buttonList) {
    		abcPanel.add(button);
    		button.addActionListener(new AbcButtonListener(i));
    		i++;
    	}
    	
    	return abcPanel;
    }
    
    /**
     * Returns the main-dictionary-tab-panel
     * @return JPanel
     */
    public JPanel getPanel() {
    	return panel;
    }
    
    private List<JButton> createAbcButtons() {
    	List<JButton> buttonList = new ArrayList<JButton>();
    	
    	for (String s : charStrings) {
    		JButton button = new JButton(s);
    		buttonList.add(button);
    	}
    	
    	return buttonList;
    }
    
    private class AbcButtonListener implements ActionListener {
    	String charString;
    	
    	public AbcButtonListener(int alphabetNumber) {
    		super();
    		charString = charStrings.get(alphabetNumber);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearTableEntries();
			for(String[] row : getDicEntries(charString)){
				model.addRow(row);
			}
			//model.setDataVector(colTitles, getDicEntries(charString));
		}
    	
    }
    
    private class NextButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			clearTableEntries();
			for(String[] row : getMoreDicEntries()){
				model.addRow(row);
			}
		}
    	
    }
    
    private JPanel createLookupPanel(){
    	JPanel lookupPanel = new JPanel(new SpringLayout());
    	
    	correctField = new JTextField("");
    	resultField = new JTextField("");
    	
    	resultField.setEditable(false);
    	
    	JButton correctButton = new JButton("Correct");
    	
    	correctButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resultField.setText(dic.correctWord(correctField.getText()));
			}
		});

    	lookupPanel.add(correctField);
    	lookupPanel.add(correctButton);
    	lookupPanel.add(resultField);
    	
    	
    	SpringUtilities.makeCompactGrid(lookupPanel, 1, 3, 5, 5, 5, 5);
    	
    	return lookupPanel;
    }

}



