package carpetclient.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DebugWindow {
    public static DebugWindow debug;
    JCheckBox chckbxStackTrace;
    Chunkgrid canvas = new Chunkgrid();

    JButton btnLoad;
    JButton btnSave;

    JTextArea textTime;
    JTextArea textX;
    JTextArea textZ;
    JComboBox comboBox;

    Controller control;
    private JFrame frame;

    public DebugWindow() {
        control = new Controller(this);
        initialize();
    }

    public void setVis(boolean v) {
        frame.setVisible(v);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 800, 800);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel panel_1 = new JPanel();
        frame.getContentPane().add(panel_1, BorderLayout.NORTH);
        panel_1.setLayout(new GridLayout(3, 1, 0, 0));

        JPanel panel_3 = new JPanel();
        panel_1.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        JButton btnStart = new JButton("Start");
        panel_3.add(btnStart);

        chckbxStackTrace = new JCheckBox("Stack Trace");
        panel_3.add(chckbxStackTrace);

        JPanel panel = new JPanel();
        panel_3.add(panel);

        btnLoad = new JButton("Load");
        panel_3.add(btnLoad);

        btnSave = new JButton("Save");
        panel_3.add(btnSave);

        JPanel panel_4 = new JPanel();
        panel_1.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        JButton btnBack = new JButton("Back");
        panel_4.add(btnBack);

//        JLabel lblNewLabel_3 = new JLabel("Time:");
//        panel_4.add(lblNewLabel_3);

        JButton btnForward = new JButton("Forward");
        panel_4.add(btnForward);

        textTime = new JTextArea();
        panel_4.add(textTime);

        JButton btnCurrent = new JButton("Current");
        panel_4.add(btnCurrent);
        btnCurrent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.current();
            }
        });

        JPanel panel_2 = new JPanel();
        panel_1.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JButton btnHome = new JButton("Home");
        panel_2.add(btnHome);

        JLabel lblNewLabel = new JLabel("X:");
        panel_2.add(lblNewLabel);

        textX = new JTextArea();
        panel_2.add(textX);
        textX.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                control.setX(e, textX);
            }
        });

        JLabel lblNewLabel_2 = new JLabel("Z:");
        panel_2.add(lblNewLabel_2);

        textZ = new JTextArea();
        panel_2.add(textZ);
        textZ.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                control.setZ(e, textZ);
            }
        });

        comboBox = new JComboBox();
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.comboBoxAction();
            }
        });
        comboBox.setModel(new DefaultComboBoxModel(new String[]{"Overworld", "Nether", "End"}));
        panel_2.add(comboBox);

        btnHome.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.home();
            }
        });
        btnForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.forward();
            }
        });
        textTime.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                control.setTime(e);
            }
        });
        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.back();
            }
        });
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.save();
            }
        });
        btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                control.load();
            }
        });
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean value = control.startStop();
                btnStart.setText(value ? "Stop" : "Start");
            }
        });

//        canvas.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent arg0) {
//                int x = arg0.getX();
//                int y = arg0.getY();
//                EventQueue.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        control.selectchunk(x, y);
//                    }
//                });
//            }
//        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        canvas.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                control.scroll(e.getWheelRotation());
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                control.selectchunk(x, y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
    }

    public boolean getStackTrace() {
        return chckbxStackTrace.isSelected();
    }

    public void setTimeTextField(String s) {
        textTime.setText(s);
    }

    public Chunkgrid getCanvas() {
        return canvas;
    }

    public void updateCanvas(int index) {
        control.liveUpdate(index);
    }

    public int getComboBoxValue() {
        return comboBox.getSelectedIndex();
    }

    public void disableSaveLoadButtons(boolean start) {
        btnLoad.setEnabled(!start);
        btnSave.setEnabled(!start);
    }

    public void setXTest(String s) {
        textX.setText(s);
    }

    public void setZText(String s) {
        textZ.setText(s);
    }
}