package ca._4976.steamworks.subsystems.profiler;

import ca._4976.library.controllers.components.Boolean;
import ca._4976.library.controllers.components.Double;
import ca._4976.library.listeners.ButtonListener;
import ca._4976.library.listeners.RobotStateListener;
import ca._4976.library.listeners.StringListener;
import ca._4976.steamworks.Robot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MotionControl {

    public Playback playback;
    public Record record;
    private SaveFile saveFile = new SaveFile();
    private NetworkTable table = NetworkTable.getTable("Motion Control");
	private Log log = null;

    public MotionControl(Robot robot) {

    	Config.getInstance().setListener(() -> {

		    String load = Config.getInstance().loadTable;

		    if (!load.equals("")) {

			    System.out.println("<Motion Control> Getting autonomous: " + load);
			    playback.setProfile(saveFile.load(load));
		    }

		    else System.out.println("<Motion Control> Successfully set autonomous to last record.");
	    });

        playback = new Playback(robot);
	    record = new Record(robot);

        Boolean disable = new Boolean(0) { @Override public boolean get() { return false; }};

        disable.addListener(new ButtonListener() {

            @Override public void pressed() { playback.disableDrive(); }
        });

        Boolean[] buttons = new Boolean[] {
                robot.driver.A,
                robot.driver.B,
                robot.driver.X,
                robot.driver.Y,
                robot.driver.RB,
                robot.driver.LB,
                robot.driver.BACK,
                robot.driver.START,
                robot.driver.LS,
                robot.driver.RS,
                robot.operator.A,
                robot.operator.B,
                robot.operator.X,
                robot.operator.Y,
                robot.operator.RB,
                robot.operator.LB,
                robot.operator.BACK,
                robot.operator.START,
                robot.operator.LS,
                robot.operator.RS,
                disable,
                robot.operator.UP,
                robot.operator.UP_LEFT,
                robot.operator.UP_RIGHT,
                robot.operator.LEFT,
                robot.operator.RIGHT,
                robot.operator.DOWN,
                robot.operator.DOWN_LEFT,
                robot.operator.DOWN_RIGHT
        };

        record.setButtons(buttons);
        saveFile.changeControllerRecordPresets(buttons);

        Double[] axes = new Double[] {
                robot.driver.LV,
                robot.driver.RH,
                robot.driver.RV,
                robot.operator.LH,
                robot.operator.LV,
                robot.operator.RH,
                robot.operator.RV,
                robot.operator.LT,
                robot.operator.RT,
        };

        record.setAxes(axes);
        saveFile.changeControllerRecordPresets(axes);

        table.putString("load_table", "");

        robot.driver.BACK.addListener(new ButtonListener() {

            @Override public void pressed() {

                robot.inputs.driveLeft.reset();
                robot.inputs.driveRight.reset();

                synchronized (this) { new Thread(record).start(); }
            }
        });

        robot.addListener(new RobotStateListener() {

            @Override public void robotInit() { table.putStringArray("table", new SaveFile().getFileNames()); }

            @Override public void disabledInit() { table.putStringArray("table", new SaveFile().getFileNames()); }

            @Override public void autonomousInit() {

                if (table.getString("load_table", "").equals("")) playback.setProfile(record.getProfile());

                robot.inputs.driveLeft.reset();
                robot.inputs.driveRight.reset();

                synchronized (this) {

                    try {

                    	log = new Log();

                        playback.setListener(log);

                    } catch (Exception e) { e.printStackTrace(); }

                    playback.reset();
                    playback.start();
                }
            }

            @Override public void testInit() {

                synchronized (this) {

                    try {

                        playback.setListener(new StringListener() {

                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                            Date date = new Date();

                            String file = "/home/lvuser/motion/Record " + dateFormat.format(date) + ".csv";

                            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file)));

                            @Override public void append(String string) {

                                try {

                                    writer.write(string);
                                    writer.newLine();
                                    writer.flush();

                                } catch (IOException e) { e.printStackTrace(); }
                            }
                        });

                    } catch (Exception e) { e.printStackTrace(); }

                    robot.enableOperatorControl();

                    record.reset();
                    record.start();
                }
            }
        });
    }

    public Log getLog() { return log; }

    private class Log implements StringListener {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String file = "/home/lvuser/motion/logs/Log " + dateFormat.format(date) + ".csv";
		BufferedWriter writer;

	    Log() {

			try {

				writer = new BufferedWriter(new FileWriter(new File(file)));

				writer.write("Profile " + NetworkTable.getTable("Motion Control ").getString("load_table", "") + ",,,Actual,");
				writer.newLine();
				writer.write("Left Output,Right Output,Left Position,Right Position,");
				writer.write("Left Output,Right Output,Left Position,Right Position,Left Error,Right Error");
				writer.newLine();
				writer.flush();

			} catch (IOException e) { e.printStackTrace(); }
		}

		@Override public void append(String string) {

			try {

				writer.write(string);
				writer.newLine();
				writer.flush();

			} catch (IOException e) { e.printStackTrace(); }
		}
	}
}
