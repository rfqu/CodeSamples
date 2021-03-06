/*
 * Copyright 2011 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package so.swing;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * To include GUI into dataflow graph, the only requirement is:
 *   GUI actors should run on a SwingExecutor.
 * 
 * EDT (JTextField) -> Executor (computing actor) -> EDT (printing actor)
 */
@SuppressWarnings("serial")
public class SwingActorTest3 {

    static class GUI extends JFrame {
        JTextArea jlist = new JTextArea();

        public GUI(CompletableFuture<GUI> result) {
            this.setTitle("SwingActor Test");
            this.setSize(360, 300);
            this.getContentPane().setLayout(null);

            jlist.setBounds(34, 120, 200, 120);
            jlist.setBorder(new LineBorder(Color.BLACK));
            this.add(jlist, null);

            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(WindowEvent winEvt) {
                    System.exit(0);
                }
            });
            super.setVisible(true);
            result.complete(this);
        }

        public void print(String m) {
            jlist.append(m);
        }

        public class MySwingWorker extends SwingWorker<Long, Void> {

            @Override
            protected java.lang.Long doInBackground() throws Exception {
                return System.currentTimeMillis();
            }

            @Override
            protected void done() {
                long time = 0;
                try {
                    time = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                print("time="+time);
            }
        }

    }

    public static void main (String[]args) throws Exception {
        CompletableFuture<GUI> result = new CompletableFuture();
        EventQueue.invokeLater(() -> new GUI(result));
        GUI gui = result.get();
        GUI.MySwingWorker swingWorker = gui.new MySwingWorker();
        swingWorker.run();
        System.out.println("main ends");
    }
}