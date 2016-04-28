/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import org.jtregext.GuiTestListener;
import com.sun.swingset3.demos.splitpane.SplitPaneDemo;
import static com.sun.swingset3.demos.splitpane.SplitPaneDemo.*;
import java.awt.event.KeyEvent;
import javax.swing.JSplitPane;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import static org.jemmy2ext.JemmyExt.*;
import org.testng.annotations.Listeners;

/*
 * @test
 * @key headful
 * @summary Verifies SwingSet3 SplitPaneDemo by performing OneClick expansion,
 *          changing size of the divier, moving the divider to different positions
 *          and changing the divider orientation.
 *
 * @library /sanity/client/lib/jemmy/src
 * @library /sanity/client/lib/Extensions/src
 * @library /sanity/client/lib/SwingSet3/src
 * @build org.jemmy2ext.JemmyExt
 * @build com.sun.swingset3.demos.splitpane.SplitPaneDemo
 * @run testng SplitPaneDemoTest
 */
@Listeners(GuiTestListener.class)
public class SplitPaneDemoTest {

    @Test
    public void test() throws Exception {

        new ClassReference(SplitPaneDemo.class.getCanonicalName()).startApplication();

        JFrameOperator frame = new JFrameOperator(DEMO_TITLE);

        JSplitPaneOperator splitPane = new JSplitPaneOperator(frame);

        // Toggle OneTouch Expandable
        checkOneTouch(frame, splitPane, true);
        checkOneTouch(frame, splitPane, false);

        // Check changing divider size to minimum and maximum values
        changeDividerSize(frame, splitPane, 50);
        changeDividerSize(frame, splitPane, 6);

        // Check moving the divider
        checkDividerMoves(frame, splitPane, false);
        checkDividerMoves(frame, splitPane, true);

        // Check different minumum Day/Night sizes
        changeMinimumSizes(frame, splitPane, 100);
        changeMinimumSizes(frame, splitPane, 0);
    }

    // Check for different day and night minimum size
    public void changeMinimumSizes(JFrameOperator frame, JSplitPaneOperator splitPane, int amount) throws Exception {
        for (String label : new String[]{FIRST_COMPONENT_MIN_SIZE, SECOND_COMPONENT_MIN_SIZE}) {
            JTextFieldOperator size = new JTextFieldOperator(getLabeledContainerOperator(frame, label));
            size.enterText(Integer.toString(amount));
            size.pressKey(KeyEvent.VK_ENTER);
        }
        checkDividerMoves(frame, splitPane, false);
        checkDividerMoves(frame, splitPane, true);
    }

    // Check moving of divider
    public void checkDividerMoves(JFrameOperator frame, JSplitPaneOperator splitPane, boolean isVertical) throws Exception {
        if (isVertical) {
            new JRadioButtonOperator(frame, VERTICAL_SPLIT).doClick();
        } else {
            new JRadioButtonOperator(frame, HORIZONTAL_SPLIT).doClick();
        }

        splitPane.moveDivider(0.0);
        assertEquals("Move Minimum, dividerLocation is at minimumDividerLocation",
                splitPane.getMinimumDividerLocation(), splitPane.getDividerLocation());

        // use getMaximumDividerLocation() to move divider to here because using proportion 1.0 does not work
        splitPane.moveDivider(1.0);

        assertEquals("Move Maximum, dividerLocation is at maximumDividerLocation",
                splitPane.getMaximumDividerLocation(), splitPane.getDividerLocation());

        splitPane.moveDivider(0.5);
        assertEquals("Move Middle, dividerLocation is at the artithmetic average of minimum and maximum DividerLocations",
                (splitPane.getMaximumDividerLocation() + splitPane.getMinimumDividerLocation()) / 2, splitPane.getDividerLocation());
    }

    // Check changing the size of the divider
    public void changeDividerSize(JFrameOperator frame, JSplitPaneOperator splitPane, int amount) throws Exception {
        JTextFieldOperator size = new JTextFieldOperator(getLabeledContainerOperator(frame, DIVIDER_SIZE));
        size.clearText();
        size.typeText(Integer.toString(amount));
        size.pressKey(KeyEvent.VK_ENTER);

        assertEquals("Change Divider Size", amount, splitPane.getDividerSize());
    }

    public void checkOneTouch(JFrameOperator frame, JSplitPaneOperator splitPane, boolean oneTouch) throws Exception {
        JCheckBoxOperator checkBox = new JCheckBoxOperator(frame, ONE_TOUCH_EXPANDABLE);
        JButtonOperator buttonLeft = new JButtonOperator(splitPane.getDivider(), 0);
        JButtonOperator buttonRight = new JButtonOperator(splitPane.getDivider(), 1);
        int initDividerLocation = splitPane.getDividerLocation();

        if (oneTouch) {
            if (!checkBox.isSelected()) {
                // uncheck
                checkBox.doClick();
            }

            int left = getUIValue(splitPane, (JSplitPane sp) -> sp.getInsets().left);
            System.out.println("left = " + left);
            int right = getUIValue(splitPane, (JSplitPane sp) -> sp.getInsets().right);
            System.out.println("right = " + right);

            // expand full left
            buttonLeft.push();
            assertEquals("Expandable Left", left, splitPane.getDividerLocation());

            // expand back from full left
            buttonRight.push();
            assertEquals("Expandable Back to Original from Left",
                    initDividerLocation, splitPane.getDividerLocation());

            // expand all the way right
            buttonRight.push();
            assertEquals("Expandable Right",
                    splitPane.getWidth() - splitPane.getDividerSize() - right,
                    splitPane.getDividerLocation());

            // Click to move back from right expansion
            buttonLeft.push();
            assertEquals("Expandable Back to Original from Right",
                    initDividerLocation, splitPane.getDividerLocation());
        }

        // Test for case where one touch expandable is disabled
        if (!oneTouch) {
            if (checkBox.isSelected()) {
                // uncheck
                checkBox.doClick();
            }
            assertFalse("One Touch Expandable Off", splitPane.isOneTouchExpandable());
        }
    }

}
