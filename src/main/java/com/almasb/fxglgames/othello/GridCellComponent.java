package com.almasb.fxglgames.othello;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.almasb.fxgl.dsl.components.view.ChildViewComponent;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppHeight;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppWidth;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */

    public class GridCellComponent extends ChildViewComponent {
        private double x;
        private double y;

        public GridCellComponent(double x, double y) {
            Rectangle bg = new Rectangle(x * 5, y * 5, Color.rgb(13, 222, 236));

            this.x = getX();
            this.y = getY();
            getViewRoot().getChildren().addAll(new StackPane(bg));
        }
    }

