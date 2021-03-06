package guis;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

import application.Controller;
import objects.ParkingLot;
/**
 * File Name: GUIReport.java
 * UWF Parking App
 *
 * This class handles the gui for the Create Parking Report page.
 *
 * @author Nathan, Will
 * @version 1.0
 */
public class GUIReport extends JPanel
{
    /** the controller to call this class */
    private Controller controller;

    /** temporary lot object to add to the database */
    private ParkingLot lot;

    /** contains the lot's statistics */
    private JPanel stats;

    /** the spaces available after entering in the number of cars */
    private JLabel spaceAvail;

    /** the number of cars to input */
    private JLabel numCars;

    /** the number of violations to input */
    private JLabel violations;

    /** map of the lot specified */
    private JLabel lotImage;

    /**
     * Constructor that prepares the gui
     * @param controller the program controller
     */
    public GUIReport (Controller controller)
    {
        this.controller = controller;
        this.lotImage = new JLabel("");
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.initializeStats();
        this.addComponents();
        this.add(lotImage);
        this.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    /** Keep the statistics blank until a lot is found */
    private void initializeStats ()
    {
        stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.PAGE_AXIS));
        spaceAvail = new JLabel("Spaces Available: --");
        numCars = new JLabel("Number of Cars: --");
        violations = new JLabel("Violations: --");
        stats.add(spaceAvail);
        stats.add(numCars);
        stats.add(violations);
        stats.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(stats);
    }

    /** show the statistics of a valid lot */
    private void viewStats ()
    {
        spaceAvail.setText("Spaces Available: " + lot.getAvailable());
        numCars.setText("Number of Cars: " + lot.getOccupied());
        violations.setText("Violations: " + lot.getViolations());
        this.revalidate();
        this.repaint();
    }

    /** update the statistics of a valid lot */
    private void updateStats ()
    {
        JPanel update = new JPanel();
        update.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel carLabel = new JLabel("Number of Cars: ");
        JTextField carField = new JTextField(20);

        JLabel violationLabel = new JLabel("Violations: ");
        JTextField violationField = new JTextField(20);

        JButton btnUpdate = new JButton("Update Statistics");
        btnUpdate.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    int space = Integer.parseInt(carField.getText());
                    int violations = Integer.parseInt(violationField.getText());

                    lot = controller.updateLot(lot.getLotID(), space, violations);
                    if (lot != null)
                    {
                        controller.displayError("Report was successfully submitted");
                        viewStats();
                    }
                    if (lot == null)
                    {
                        controller.displayError("An error occurred while updating the lot's information");
                    }
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    controller.displayError("An error occurred while updating the lot's information");
                }
            }
        });

        update.add(carLabel);
        update.add(carField);
        update.add(violationLabel);
        update.add(violationField);
        update.add(btnUpdate);

        this.add(update);

        this.revalidate();
        this.repaint();
    }

    /*private void addStatistics()
    {
        JPanel componentPanel = new JPanel();

        JLabel spaceAvail = new JLabel("Enter # of spaces available: ");
        JTextField numSpaces = new JTextField(5);

        JButton btnAddStats = new JButton("Add Statistics");
        btnAddStats.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int space = Integer.parseInt(numSpaces.getText());
                ParkingLot result = controller.updateLotCars(parkingLotID, space);
                if (result != null)
                {
                    controller.displayError("Report was successfully submitted");
                }
                if (result == null)
                {
                    controller.displayError("An error occurred while updating lot information");
                }
            }
        });

        componentPanel.add(spaceAvail);
        componentPanel.add(numSpaces);
        componentPanel.add(btnAddStats);

        this.add(componentPanel);
        this.revalidate();
    }*/

    /** adds the gui elements */
    private void addComponents ()
    {
        JPanel componentPanel = new JPanel();

        JLabel lotID = new JLabel("Lot ID:");
        JTextField lotField = new JTextField(20);

        JButton btnViewLot = new JButton("View Parking Lot");
        btnViewLot.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                lot = controller.getLot(lotField.getText());
                if (lot == null)
                    controller.displayError("There is no lot with that ID");
                else if (lotField.getText().equals(""))
                    controller.displayError("There is not a lot specified");
                else
                {
                    drawParkingLot(lot.getLotID().toLowerCase());
                    viewStats();
                }
                repaint();
            }
        });

        componentPanel.add(lotID);
        componentPanel.add(lotField);
        componentPanel.add(btnViewLot);

        JButton btnGoBack = new JButton("Go Back");
        btnGoBack.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                controller.displayGUIMainMenu();
            }
        });

        this.add(componentPanel);
        this.updateStats();
        this.add(btnGoBack);
    }

    /**
     * Pull up a map of the lot specified
     * @param lotID name of the lot
     */
    private void drawParkingLot (String lotID)
    {
        BufferedImage bi = null;
        try
        {
            bi = ImageIO.read(new File(String.format("resources/%s_lot.png", lotID.toLowerCase())));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Image img = bi.getScaledInstance(this.getWidth()/2, this.getHeight()/2, Image.SCALE_FAST);
        ImageIcon icon = new ImageIcon(img);
        lotImage.setIcon(icon);
        lotImage.setAlignmentX(this.CENTER_ALIGNMENT);
        this.revalidate();
    }
}