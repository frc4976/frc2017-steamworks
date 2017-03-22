package ca._4976.steamworks.subsystems;

import ca._4976.library.listeners.ButtonListener;
import ca._4976.steamworks.Robot;

public class Winch {

    public Winch(Robot robot) {

        robot.driver.Y.addListener(new ButtonListener() {

            @Override public void falling() {

                robot.outputs.winchMaster.set(robot.outputs.winchMaster.get() == 0 ? -1 : 0);
            }
        });

        robot.operator.RV.addListener(value -> {

            if (Math.abs(value) < 0.1) robot.outputs.winchMaster.set(0);

            else robot.outputs.winchMaster.set(value);
        });

        robot.operator.Y.addListener(new ButtonListener() {

            @Override public void falling() { robot.outputs.arch.output(!robot.outputs.arch.isExtened());
            }
        });
    }
}
