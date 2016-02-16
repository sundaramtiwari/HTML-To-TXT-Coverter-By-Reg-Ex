package client;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

class ResponseTimeExtractor extends JFrame {

 private static final long serialVersionUID = 1L;

 String emailableLocation, textLocation;

 JButton startConversion, openFile;
 JTextField emailableTextField, textTextField;
 JLabel emailableLabel, textLabel, result;

 ResponseTimeExtractor() {

  JFrame frame = new JFrame("Response Time");
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  frame.setSize(600, 200);
  Container container = frame.getContentPane();
  container.setLayout(new FlowLayout(FlowLayout.CENTER));

  /** Setting the UI Content **/
  // Location of the HTML file on system
  emailableLabel = new JLabel("HTML File Location");

  // Location to create the corresponding Text File on system
  textLabel = new JLabel("Text File Location");

  emailableTextField = new JTextField(40);
  textTextField = new JTextField(40);
  result = new JLabel("");
  startConversion = new JButton("Start Extraction");
  openFile = new JButton("Open Text File");

  // Add the components to container
  container.add(emailableLabel);
  container.add(emailableTextField);
  container.add(textLabel);
  container.add(textTextField);
  container.add(startConversion);
  container.add(result);
  container.add(openFile);
  frame.setVisible(true);

  // Add action event to start extraction of response time from html file
  startConversion.addActionListener(new ActionListener() {

   public void actionPerformed(ActionEvent e) {
    emailableLocation = emailableTextField.getText();
    textLocation = textTextField.getText();
    try {
     result.setText(" ");
     getApi(emailableLocation, textLocation);
    } catch (IOException e1) {
     // TODO Auto-generated catch block
     e1.printStackTrace();
    }
   }

  });

  // Action event to open the created text file
  openFile.addActionListener(new ActionListener() {

   public void actionPerformed(ActionEvent e) {
    openTextFile(textLocation);
   }

  });

 }

 public static void main(String args[]) {

  new ResponseTimeExtractor();

 }

 /**
  * This method opens the create Text File
  *
  * @param textLocation
  */
 private void openTextFile(String textLocation) {
  // TODO Auto-generated method stub
  if (Desktop.isDesktopSupported()) {
   try {
    Desktop.getDesktop().open(new File(textLocation));
   } catch (IOException e) { /* TODO: error handling */
   }
  } else { /* TODO: error handling */
   result.setText("Cannot Open the file");
  }
 }

 /**
  * This method extracts response time from the html file and writes it to
  * the txt file
  *
  * @param emailableLocation
  * @param textLocation
  * @throws IOException
  */
 @SuppressWarnings("deprecation")
 public void getApi(String emailableLocation, String textLocation)
   throws IOException {

  try {

   result.setText("Error Occured!!!");
   // HTML file handling
   File myhtmlFile = new File(emailableLocation);
   FileInputStream fileinput = null;
   BufferedInputStream mybuffer = null;
   DataInputStream datainput = null;

   fileinput = new FileInputStream(myhtmlFile);
   mybuffer = new BufferedInputStream(fileinput);
   datainput = new DataInputStream(mybuffer);

   // Map to store API name and corresponding response time
   HashMap<String, Integer> hm = new HashMap<String, Integer>();

   // Read from HTML file for Response Time of all the API's
   while (datainput.available() != 0) {
    String data = datainput.readLine();
    Pattern regex = Pattern.compile("(--)(.*)(msec)");
    Matcher regexMatcher = regex.matcher(data);

    if (regexMatcher.find()) {
     String string = regexMatcher.group();
     System.out.println(string);
     if(string.length()>100)
      continue;
     else{ 
     String[] strArr = string.split(":");
     String apiName = strArr[0].replaceFirst("--", "").trim();
     String timeMsec = strArr[1].trim().replace("msec", "");
     
     System.out.println(apiName);
     System.out.println(timeMsec);
     if (!hm.containsKey(apiName)) {
      if(timeMsec.endsWith("msec")){
       int time = Integer.parseInt(timeMsec);
       hm.put(apiName, time);
      }
     }

     else {
      int timeNew = Integer.parseInt(timeMsec);
      int timeOld = hm.get(apiName);
      if (timeNew > timeOld) {
       hm.put(apiName, timeNew);
      }
     }
     }
    }
   }

   // Text File Handling
   FileWriter fstream = new FileWriter(textLocation);
   BufferedWriter out = new BufferedWriter(fstream);

   // Write data to Text File
   Set<String> set = hm.keySet();
   for (String key : set) {
    out.write(key + ": " + hm.get(key) + " ms");
    out.newLine();
   }

   // UI alert for successful read & write operation
   result.setText("File Created Successfully!!!");

   // Closing the streams
   out.close();
   fileinput.close();
   mybuffer.close();
   datainput.close();

  } catch (Exception e) {
   // TODO: handle exception
   e.printStackTrace();
  }
 }
}