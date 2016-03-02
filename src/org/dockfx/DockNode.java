/**
 * @file DockNode.java
 * @brief Class implementing basic dock node with floating and styling.
 *
 * @section License
 *
 *          This file is a part of the DockFX Library. Copyright (C) 2015 Robert B. Colton
 *
 *          This program is free software: you can redistribute it and/or modify it under the terms
 *          of the GNU Lesser General Public License as published by the Free Software Foundation,
 *          either version 3 of the License, or (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *          WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *          PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 *          You should have received a copy of the GNU Lesser General Public License along with this
 *          program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.dockfx;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.dockfx.events.DockNodeEvent;
import org.dockfx.events.DockNodeEventListenerInterface;
import org.dockfx.viewControllers.BaseViewController;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Base class for a dock node that provides the layout of the content along with
 * a title bar and a styled border. The dock node can be detached and floated or
 * closed and removed from the layout. Dragging behavior is implemented through
 * the title bar.
 *
 * @since DockFX 0.1
 */
public class DockNode extends VBox implements EventHandler<MouseEvent> {
	/**
	 * The style this dock node should use on its stage when set to floating.
	 */
	private StageStyle stageStyle = StageStyle.TRANSPARENT;
	/**
	 * The stage that this dock node is currently using when floating.
	 */
	private Stage stage;

	/**
	 * The contents of the dock node, i.e. a TreeView or ListView.
	 */
	private Node contents;
	/**
	 * The title bar that implements our dragging and state manipulation.
	 */
	private DockTitleBar dockTitleBar;
	/**
	 * The border pane used when floating to provide a styled custom border.
	 */
	private BorderPane borderPane;

	/**
	 * The dock pane this dock node belongs to when not floating.
	 */
	private DockPane dockPane;

	/**
	 * View controller of node inside this DockNode
	 */
	private BaseViewController viewController;

	/**
	 * Cursor state that currentCursor is in when not resizing.
	 */
	private Cursor currentCursor;

	/**
	 * CSS pseudo class selector representing whether this node is currently
	 * floating.
	 */
	private static final PseudoClass FLOATING_PSEUDO_CLASS = PseudoClass.getPseudoClass("floating");
	/**
	 * CSS pseudo class selector representing whether this node is currently
	 * docked.
	 */
	private static final PseudoClass DOCKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("docked");
	/**
	 * CSS pseudo class selector representing whether this node is currently
	 * maximized.
	 */
	private static final PseudoClass MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");

	/**
	 * Contains listeners for DockNode events
	 */
	private Set<DockNodeEventListenerInterface> listeners = new HashSet<DockNodeEventListenerInterface>();

	/**
	 * Boolean property maintaining whether this node is currently maximized.
	 *
	 * @defaultValue false
	 */
	private BooleanProperty maximizedProperty = new SimpleBooleanProperty(false) {

		@Override
		protected void invalidated() {
			DockNode.this.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
			if (borderPane != null) {
				borderPane.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
			}

			stage.setMaximized(get());

			// TODO: This is a work around to fill the screen bounds and not
			// overlap the task bar when
			// the window is undecorated as in Visual Studio. A similar work
			// around needs applied for
			// JFrame in Swing.
			// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4737788
			// Bug report filed:
			// https://bugs.openjdk.java.net/browse/JDK-8133330
			if (this.get()) {
				Screen screen = Screen
						.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).get(0);
				Rectangle2D bounds = screen.getVisualBounds();

				stage.setX(bounds.getMinX());
				stage.setY(bounds.getMinY());

				stage.setWidth(bounds.getWidth());
				stage.setHeight(bounds.getHeight());
			}
		}

		@Override
		public String getName() {
			return "maximized";
		}
	};

	/**
	 * Creates a default DockNode with a default title bar and layout.
	 *
	 * @param contents
	 *            The contents of the dock node which may be a tree or another
	 *            scene graph node.
	 * @param title
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 * @param graphic
	 *            The caption graphic of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 */
	public DockNode(Node contents, String title, Node graphic) {
		initializeDockNode(contents, title, graphic);
	}

	/**
	 * Creates a default DockNode with a default title bar and layout.
	 *
	 * @param contents
	 *            The contents of the dock node which may be a tree or another
	 *            scene graph node.
	 * @param title
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 */
	public DockNode(Node contents, String title) {
		this(contents, title, null);
	}

	/**
	 * Creates a default DockNode with a default title bar and layout.
	 *
	 * @param contents
	 *            The contents of the dock node which may be a tree or another
	 *            scene graph node.
	 */
	public DockNode(Node contents) {
		this(contents, null, null);
	}

	/**
	 *
	 * Creates a default DockNode with contents loaded from FXMLFile at provided
	 * path.
	 *
	 * @param FXMLPath
	 *            path to fxml file.
	 * @param title
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 * @param graphic
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 */
	public DockNode(String FXMLPath, String title, Node graphic) {
		FXMLLoader loader = loadNode(FXMLPath);
		initializeDockNode(loader.getRoot(), title, graphic);
		viewController = loader.getController();
		currentCursor = Cursor.DEFAULT;
	}

	/**
	 * Creates a default DockNode with contents loaded from FXMLFile at provided
	 * path.
	 *
	 * @param FXMLPath
	 *            path to fxml file.
	 * @param title
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 */
	public DockNode(String FXMLPath, String title) {
		this(FXMLPath, title, null);
	}

	/**
	 * Creates a default DockNode with contents loaded from FXMLFile at provided
	 * path with default title bar.
	 *
	 * @param FXMLPath
	 *            path to fxml file.
	 */
	public DockNode(String FXMLPath) {
		this(FXMLPath, null, null);
	}

	/**
	 * Loads Node from fxml file located at FXMLPath and returns it.
	 *
	 * @param FXMLPath
	 *            Path to fxml file.
	 * @return Node loaded from fxml file or StackPane with Label with error
	 *         message.
	 */
	private static FXMLLoader loadNode(String FXMLPath) {
		FXMLLoader loader = new FXMLLoader();
		try {
			loader.load(DockNode.class.getResourceAsStream(FXMLPath));
		} catch (Exception e) {
			e.printStackTrace();
			loader.setRoot(new StackPane(new Label("Could not load FXML file")));
		}
		return loader;
	}

	/**
	 * Sets DockNodes contents, title and title bar graphic
	 *
	 * @param contents
	 *            The contents of the dock node which may be a tree or another
	 *            scene graph node.
	 * @param title
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 * @param graphic
	 *            The caption title of this dock node which maintains
	 *            bidirectional state with the title bar and stage.
	 */
	private void initializeDockNode(Node contents, String title, Node graphic) {
		this.titleProperty.setValue(title);
		this.graphicProperty.setValue(graphic);
		this.contents = contents;

		dockTitleBar = new DockTitleBar(this);

		getChildren().addAll(dockTitleBar, contents);
		VBox.setVgrow(contents, Priority.ALWAYS);

		this.getStyleClass().add("dock-node");
	}

	/**
	 * Adds new event lister for DockNode events.
	 *
	 * @param eventListener
	 *            Listener to add.
	 */
	public void addEventListener(DockNodeEventListenerInterface eventListener) {
		listeners.add(eventListener);
	}

	/**
	 * Removes event listener from DockNode eventListeners.
	 *
	 * @param eventListener
	 *            Listener to remove.
	 */
	public void removeEventListener(DockNodeEventListenerInterface eventListener) {
		listeners.remove(eventListener);
	}

	/**
	 * Fires DockNode close event.
	 */
	private void fireCloseEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeClosed(e);
		}
	}

	/**
	 * Fires DockNode maximize event
	 */
	private void fireMaximizeEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeMaximized(e);
		}
	}

	/**
	 * Fires DockNode windowed event
	 */
	private void fireWindowEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeWindowed(e);
		}
	}

	/**
	 * Fires DockNode minimize event
	 */
	private void fireMinimizeEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeMinimized(e);
		}
	}

	private void fireRestoreEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeRestored(e);
		}
	}

	/**
	 * Fires DockNode dock event
	 */
	private void fireDockEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeDocked(e);
		}
	}

	/**
	 * Fires DockNode float event
	 */
	private void fireFloatEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeFloated(e);
		}
	}

	private void fireFocusedEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeFocused(e);
		}
	}

	private void fireDefocusedEvent() {
		DockNodeEvent e = new DockNodeEvent(this);
		for (DockNodeEventListenerInterface listener : listeners) {
			listener.dockNodeDefocused(e);
		}
	}

	/**
	 * The stage style that will be used when the dock node is floating. This
	 * must be set prior to setting the dock node to floating.
	 *
	 * @param stageStyle
	 *            The stage style that will be used when the node is floating.
	 */
	public void setStageStyle(StageStyle stageStyle) {
		this.stageStyle = stageStyle;
	}

	/**
	 * Changes the contents of the dock node.
	 *
	 * @param contents
	 *            The new contents of this dock node.
	 */
	public void setContents(Node contents) {
		this.getChildren().set(this.getChildren().indexOf(this.contents), contents);
		this.contents = contents;
	}

	/**
	 * Changes the title bar in the layout of this dock node. This can be used
	 * to remove the dock title bar from the dock node by passing null.
	 *
	 * @param dockTitleBar
	 *            null The new title bar of this dock node, can be set null
	 *            indicating no title bar is used.
	 */
	public void setDockTitleBar(DockTitleBar dockTitleBar) {
		if (dockTitleBar != null) {
			if (this.dockTitleBar != null) {
				this.getChildren().set(this.getChildren().indexOf(this.dockTitleBar), dockTitleBar);
			} else {
				this.getChildren().add(0, dockTitleBar);
			}
			setFloatable(true);
		} else {
			this.getChildren().remove(this.dockTitleBar);
			setFloatable(false);
		}

		this.dockTitleBar = dockTitleBar;
	}

	/**
	 * Whether the node is currently maximized.
	 *
	 * @param maximized
	 *            Whether the node is currently maximized.
	 */
	public final void setMaximized(boolean maximized) {
		if (isMaximizable() && !isMaximized() && maximized) {
			fireMaximizeEvent();
			maximizedProperty.set(true);
		} else if (!maximized && isMaximized()) {
			fireWindowEvent();
			maximizedProperty.set(false);
		}
	}

	public void floatNode(Point2D translation, DockPane dockPane) {
		floatNode(translation, dockPane, false);
	}

	public void floatNode(DockPane dockPane, boolean centerInStage) {
		floatNode(null, dockPane, centerInStage);
	}

	public void floatNode(Point2D translation, DockPane dockPane, boolean centerInStage) {
		// position the new stage relative to the old scene offset
		Point2D floatScene = this.localToScene(0, 0);
		Point2D floatScreen = this.localToScreen(0, 0);

		// setup window stage
		dockTitleBar.setVisible(this.isCustomTitleBar());
		dockTitleBar.setManaged(this.isCustomTitleBar());

		if (this.isDocked()) {
			this.undock();
		} else {
			dockPane.floatNode(this);
		}

		this.dockPane = dockPane;

		stage = new Stage();
		stage.titleProperty().bind(titleProperty);
		if (dockPane != null && dockPane.getScene() != null && dockPane.getScene().getWindow() != null) {
			stage.initOwner(dockPane.getScene().getWindow());
		}

		stage.initStyle(stageStyle);

		// offset the new stage to cover exactly the area the dock was local to
		// the scene
		// this is useful for when the user presses the + sign and we have no
		// information
		// on where the mouse was clicked
		Point2D stagePosition;
		if (this.isDecorated()) {
			Window owner = stage.getOwner();
			stagePosition = floatScene.add(new Point2D(owner.getX(), owner.getY()));
		} else {
			if (floatScreen == null) {
				stagePosition = new Point2D(dockPane.getScene().getWindow().getX(),
						dockPane.getScene().getWindow().getY());
			} else {
				stagePosition = floatScreen;
			}
		}
		if (translation != null) {
			stagePosition = stagePosition.add(translation);
		}

		// the border pane allows the dock node to
		// have a drop shadow effect on the border
		// but also maintain the layout of contents
		// such as a tab that has no content
		borderPane = new BorderPane();
		borderPane.getStyleClass().add("dock-node-border");
		borderPane.setCenter(this);

		Scene scene = new Scene(borderPane);

		// apply the floating property so we can get its padding size
		// while it is floating to offset it by the drop shadow
		// this way it pops out above exactly where it was when docked
		this.floatingProperty.set(true);
		this.applyCss();

		// apply the border pane css so that we can get the insets and
		// position the stage properly
		borderPane.applyCss();
		Insets insetsDelta = borderPane.getInsets();

		double insetsWidth = insetsDelta.getLeft() + insetsDelta.getRight();
		double insetsHeight = insetsDelta.getTop() + insetsDelta.getBottom();

		stage.setMinWidth(borderPane.minWidth(this.getHeight()) + insetsWidth);
		stage.setMinHeight(borderPane.minHeight(this.getWidth()) + insetsHeight);

		borderPane.setPrefSize(this.getWidth() + insetsWidth, this.getHeight() + insetsHeight);
		borderPane.setPrefSize(this.getPrefWidth(), this.getPrefHeight());

		if (centerInStage && dockPane != null && dockPane.getScene() != null && getContents() instanceof Region) {
			Region region = (Region) getContents();
			stagePosition = stagePosition.add((dockPane.getScene().getWidth() / 2) - (region.getPrefWidth() / 2),
					(dockPane.getScene().getHeight() / 2) - (region.getPrefHeight() / 2));
		}

		stage.setX(stagePosition.getX() - insetsDelta.getLeft());
		stage.setY(stagePosition.getY() - insetsDelta.getTop());
		stage.setScene(scene);

		if (stageStyle == StageStyle.TRANSPARENT) {
			scene.setFill(null);
		}

		stage.setResizable(this.isStageResizable());
		if (this.isStageResizable()) {
			stage.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
			stage.addEventFilter(MouseEvent.MOUSE_MOVED, this);
			stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
		}

		// we want to set the client area size
		// without this it subtracts the native border sizes from the scene
		// size
		stage.sizeToScene();
		stage.show();

		getDockTitleBar().getStyleClass().add("dock-title-bar-focused");
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					fireFocusedEvent();
					setCssFocused();

				} else {
					fireDefocusedEvent();
					setCssDefosed();
				}
			}
		});
		setCssFocused();
	}

	private void setCssFocused() {
		// this removes all occurrences of undesired css class in css classes
		// list
		getDockTitleBar().getStyleClass().removeIf(new Predicate<String>() {

			public boolean test(String t) {
				if (t.equals("dock-title-bar-defocused")) {
					return true;
				}
				return false;
			}
		});
		getDockTitleBar().getStyleClass().add("dock-title-bar-focused");
	}

	private void setCssDefosed() {
		// this removes all occurrences of undesired css class in css classes
		// list
		getDockTitleBar().getStyleClass().removeIf(new Predicate<String>() {

			public boolean test(String t) {
				if (t.equals("dock-title-bar-focused")) {
					return true;
				}
				return false;
			}
		});
		getDockTitleBar().getStyleClass().add("dock-title-bar-defocused");
	}

	/**
	 * Whether the node is currently floating.
	 *
	 * @param floating
	 *            Whether the node is currently floating.
	 * @param translation
	 *            null The offset of the node after being set floating. Used for
	 *            aligning it with its layout bounds inside the dock pane when
	 *            it becomes detached. Can be null indicating no translation.
	 */
	public void setFloating(boolean floating, Point2D translation) {
		if (floating && !this.isFloating()) {
			floatNode(translation, dockPane);
		} else if (!floating && this.isFloating()) {
			this.floatingProperty.set(floating);

			stage.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
			stage.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
			stage.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);

			stage.close();
		}
	}

	/**
	 * Whether the node is currently floating.
	 *
	 * @param floating
	 *            Whether the node is currently floating.
	 */
	public void setFloating(boolean floating) {
		setFloating(floating, null);
	}

	/**
	 * The dock pane that was last associated with this dock node. Either the
	 * dock pane that it is currently docked to or the one it was detached from.
	 * Can be null if the node was never docked.
	 *
	 * @return The dock pane that was last associated with this dock node.
	 */
	public final DockPane getDockPane() {
		return dockPane;
	}

	/**
	 * ViewController associated with this dock nodes contents, might be null
	 *
	 * @return ViewController associated with this dock nodes contents
	 */
	public final BaseViewController getViewController() {
		return viewController;
	}

	/**
	 * The dock title bar associated with this dock node.
	 *
	 * @return The dock title bar associated with this node.
	 */
	public final DockTitleBar getDockTitleBar() {
		return this.dockTitleBar;
	}

	/**
	 * The stage associated with this dock node. Can be null if the dock node
	 * was never set to floating.
	 *
	 * @return The stage associated with this node.
	 */
	public final Stage getStage() {
		return stage;
	}

	/**
	 * The border pane used to parent this dock node when floating. Can be null
	 * if the dock node was never set to floating.
	 *
	 * @return The stage associated with this node.
	 */
	public final BorderPane getBorderPane() {
		return borderPane;
	}

	/**
	 * The contents managed by this dock node.
	 *
	 * @return The contents managed by this dock node.
	 */
	public final Node getContents() {
		return contents;
	}

	/**
	 * Object property maintaining bidirectional state of the caption graphic
	 * for this node with the dock title bar or stage.
	 *
	 * @defaultValue null
	 */
	public final ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>() {
		@Override
		public String getName() {
			return "graphic";
		}
	};

	public final Node getGraphic() {
		return graphicProperty.get();
	}

	public final void setGraphic(Node graphic) {
		this.graphicProperty.setValue(graphic);
	}

	/**
	 * Boolean property maintaining bidirectional state of the caption title for
	 * this node with the dock title bar or stage.
	 *
	 * @defaultValue "Dock"
	 */
	public final StringProperty titleProperty() {
		return titleProperty;
	}

	private StringProperty titleProperty = new SimpleStringProperty("Dock") {
		@Override
		public String getName() {
			return "title";
		}
	};

	public final String getTitle() {
		return titleProperty.get();
	}

	public final void setTitle(String title) {
		this.titleProperty.setValue(title);
	}

	/**
	 * Boolean property maintaining whether this node is currently using a
	 * custom title bar. This can be used to force the default title bar to show
	 * when the dock node is set to floating instead of using native window
	 * borders.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty customTitleBarProperty() {
		return customTitleBarProperty;
	}

	private BooleanProperty customTitleBarProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "customTitleBar";
		}
	};

	public final boolean isCustomTitleBar() {
		return customTitleBarProperty.get();
	}

	public final void setUseCustomTitleBar(boolean useCustomTitleBar) {
		if (this.isFloating()) {
			dockTitleBar.setVisible(useCustomTitleBar);
			dockTitleBar.setManaged(useCustomTitleBar);
		}
		this.customTitleBarProperty.set(useCustomTitleBar);
	}

	/**
	 * Boolean property maintaining whether this node is currently floating.
	 *
	 * @defaultValue false
	 */
	public final BooleanProperty floatingProperty() {
		return floatingProperty;
	}

	private BooleanProperty floatingProperty = new SimpleBooleanProperty(false) {
		@Override
		protected void invalidated() {
			DockNode.this.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
			if (borderPane != null) {
				borderPane.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
			}
		}

		@Override
		public String getName() {
			return "floating";
		}
	};

	public final boolean isFloating() {
		return floatingProperty.get();
	}

	/**
	 * Boolean property maintaining whether this node is currently floatable.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty floatableProperty() {
		return floatableProperty;
	}

	private BooleanProperty floatableProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "floatable";
		}
	};

	public final boolean isFloatable() {
		return floatableProperty.get();
	}

	public final void setFloatable(boolean floatable) {
		if (!floatable && this.isFloating()) {
			this.setFloating(false);
		}
		this.floatableProperty.set(floatable);
	}

	/**
	 * Boolean property maintaining whether this node is minimizable.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty minimizableProperty() {
		return minimizableProperty;
	}

	private BooleanProperty minimizableProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "minimizable";
		}
	};

	public final boolean isMinimizable() {
		return minimizableProperty.get();
	}

	public final void setMinimizable(boolean minimizable) {
		if (minimizable && !this.isMinimizable()) {
			dockTitleBar.getChildren().add(DockTitleBar.BUTTON_POSITION_MINIMIZE, dockTitleBar.getMinimizeButton());
		} else if (!minimizable && this.isMinimizable()) {
			dockTitleBar.getChildren().remove(dockTitleBar.getMinimizeButton());
		}
		this.minimizableProperty.set(minimizable);
	}

	/**
	 * Boolean property maintaining whether this node is maximizable.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty maximizableProperty() {
		return maximizableProperty;
	}

	private BooleanProperty maximizableProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "maximizable";
		}
	};

	public final boolean isMaximizable() {
		return maximizableProperty.get();
	}

	public final void setMaximizable(boolean maximizable) {
		if (maximizable && !this.isMaximizable()) {
			dockTitleBar.getChildren().add(DockTitleBar.BUTTON_POSITION_STATE, dockTitleBar.getStateButton());
		} else if (!maximizable && this.isMaximizable()) {
			dockTitleBar.getChildren().remove(dockTitleBar.getStateButton());
		}
		this.maximizableProperty.set(maximizable);
	}

	/**
	 * Boolean property maintaining whether this node is currently minimized.
	 *
	 * @defaultValue false
	 */
	public final BooleanProperty minimizedProperty() {
		return minimizedProperty;
	}

	private BooleanProperty minimizedProperty = new SimpleBooleanProperty(false) {
		@Override
		public String getName() {
			return "minimized";
		};
	};

	public final boolean isMinimized() {
		return minimizedProperty.get();
	}

	public final void setMinimized(boolean minimized) {
		if (!minimized && isMinimized()) {
			setVisible(true);
			fireRestoreEvent();
			stage.toFront();
		} else if (minimized && !isMinimized()) {
			setFloating(true);
			setVisible(false);
			fireMinimizeEvent();
		}
		this.minimizedProperty.set(minimized);
	}

	/**
	 * Boolean property maintaining whether this node is currently closable.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty closableProperty() {
		return closableProperty;
	}

	private BooleanProperty closableProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "closable";
		}
	};

	public final boolean isClosable() {
		return closableProperty.get();
	}

	public void setClosable(boolean closeable) {
		if (closeable && !this.isClosable()) {
			dockTitleBar.getChildren().add(DockTitleBar.BUTTON_POSITION_CLOSE, dockTitleBar.getCloseButton());
		} else if (!closeable && this.isClosable()) {
			dockTitleBar.getChildren().remove(dockTitleBar.getCloseButton());
		}
	}

	/**
	 * Boolean property maintaining whether this node is currently resizable.
	 *
	 * @defaultValue true
	 */
	public final BooleanProperty resizableProperty() {
		return stageResizableProperty;
	}

	private BooleanProperty stageResizableProperty = new SimpleBooleanProperty(true) {
		@Override
		public String getName() {
			return "resizable";
		}
	};

	public final boolean isStageResizable() {
		return stageResizableProperty.get();
	}

	public final void setStageResizable(boolean resizable) {
		stageResizableProperty.set(resizable);
	}

	/**
	 * Boolean property maintaining whether this node is currently docked. This
	 * is used by the dock pane to inform the dock node whether it is currently
	 * docked.
	 *
	 * @defaultValue false
	 */
	public final BooleanProperty dockedProperty() {
		return dockedProperty;
	}

	private BooleanProperty dockedProperty = new SimpleBooleanProperty(false) {
		@Override
		protected void invalidated() {
			if (get()) {
				if (dockTitleBar != null) {
					dockTitleBar.setVisible(true);
					dockTitleBar.setManaged(true);
				}
			}

			DockNode.this.pseudoClassStateChanged(DOCKED_PSEUDO_CLASS, get());
		}

		@Override
		public String getName() {
			return "docked";
		}
	};

	public final boolean isDocked() {
		return dockedProperty.get();
	}

	public final BooleanProperty maximizedProperty() {
		return maximizedProperty;
	}

	public final boolean isMaximized() {
		return maximizedProperty.get();
	}

	public final boolean isDecorated() {
		return stageStyle != StageStyle.TRANSPARENT && stageStyle != StageStyle.UNDECORATED;
	}

	public Cursor getCurrentCursor() {
		return currentCursor;
	}

	public void setCurrentCursor(Cursor currentCursor) {
		this.currentCursor = currentCursor;
	}

	/**
	 * Dock this node into a dock pane.
	 *
	 * @param dockPane
	 *            The dock pane to dock this node into.
	 * @param dockPos
	 *            The docking position relative to the sibling of the dock pane.
	 * @param sibling
	 *            The sibling node to dock this node relative to.
	 */
	public void dock(DockPane dockPane, DockPos dockPos, Node sibling) {
		dockImpl(dockPane);
		dockPane.dock(this, dockPos, sibling);
	}

	/**
	 * Dock this node into a dock pane.
	 *
	 * @param dockPane
	 *            The dock pane to dock this node into.
	 * @param dockPos
	 *            The docking position relative to the sibling of the dock pane.
	 */
	public void dock(DockPane dockPane, DockPos dockPos) {
		dockImpl(dockPane);
		dockPane.dock(this, dockPos);
	}

	private final void dockImpl(DockPane dockPane) {
		if (isFloating()) {
			setFloating(false);
		}
		this.dockPane = dockPane;
		this.dockedProperty.set(true);
		fireDockEvent();
	}

	/**
	 * Detach this node from its previous dock pane if it was previously docked.
	 */
	public void undock() {
		if (dockPane != null) {
			dockPane.undock(this);
		}
		this.dockedProperty.set(false);
	}

	/**
	 * Close this dock node by setting it to not floating and making sure it is
	 * detached from any dock pane.
	 */
	public void close() {
		if (isFloating()) {
			setFloating(false);
		} else if (isDocked()) {
			undock();
		}
		fireCloseEvent();
	}

	/**
	 * Returns true if this node has parent is scene graph structure.
	 *
	 * @return True if node has parent in scene graph, false if not.
	 */
	public boolean hasParent() {
		boolean result = (this.getParent() == null) ? false : true;
		return result;
	}

	/**
	 * The last position of the mouse that was within the minimum layout bounds.
	 */
	private Point2D sizeLast;
	/**
	 * Whether we are currently resizing in a given direction.
	 */
	private boolean sizeWest = false, sizeEast = false, sizeNorth = false, sizeSouth = false;

	/**
	 * Gets whether the mouse is currently in this dock node's resize zone.
	 *
	 * @return Whether the mouse is currently in this dock node's resize zone.
	 */
	public boolean isMouseResizeZone() {
		return sizeWest || sizeEast || sizeNorth || sizeSouth;
	}

	@Override
	public void handle(MouseEvent event) {

		Cursor cursor = currentCursor;

		// TODO: use escape to cancel resize/drag operation like visual studio
		if (!this.isFloating() || !this.isStageResizable()) {
			return;
		}

		if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			sizeLast = new Point2D(event.getScreenX(), event.getScreenY());
		} else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
			Insets insets = borderPane.getPadding();

			sizeWest = event.getX() < insets.getLeft();
			sizeEast = event.getX() > borderPane.getWidth() - insets.getRight();
			sizeNorth = event.getY() < insets.getTop();
			sizeSouth = event.getY() > borderPane.getHeight() - insets.getBottom();

			if (sizeWest) {
				if (sizeNorth) {
					cursor = Cursor.NW_RESIZE;
				} else if (sizeSouth) {
					cursor = Cursor.SW_RESIZE;
				} else {
					cursor = Cursor.W_RESIZE;
				}
			} else if (sizeEast) {
				if (sizeNorth) {
					cursor = Cursor.NE_RESIZE;
				} else if (sizeSouth) {
					cursor = Cursor.SE_RESIZE;
				} else {
					cursor = Cursor.E_RESIZE;
				}
			} else if (sizeNorth) {
				cursor = Cursor.N_RESIZE;
			} else if (sizeSouth) {
				cursor = Cursor.S_RESIZE;
			}

			this.getScene().setCursor(cursor);
		} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && this.isMouseResizeZone()) {
			Point2D sizeCurrent = new Point2D(event.getScreenX(), event.getScreenY());
			Point2D sizeDelta = sizeCurrent.subtract(sizeLast);

			double newX = stage.getX(), newY = stage.getY(), newWidth = stage.getWidth(), newHeight = stage.getHeight();

			if (sizeNorth) {
				newHeight -= sizeDelta.getY();
				newY += sizeDelta.getY();
			} else if (sizeSouth) {
				newHeight += sizeDelta.getY();
			}

			if (sizeWest) {
				newWidth -= sizeDelta.getX();
				newX += sizeDelta.getX();
			} else if (sizeEast) {
				newWidth += sizeDelta.getX();
			}

			// TODO: find a way to do this synchronously and eliminate the
			// flickering
			// of moving the stage
			// around, also file a bug report for this feature if a work around
			// can
			// not be found this
			// primarily occurs when dragging north/west but it also appears in
			// native
			// windows and Visual
			// Studio, so not that big of a concern.
			// Bug report filed:
			// https://bugs.openjdk.java.net/browse/JDK-8133332
			double currentX = sizeLast.getX(), currentY = sizeLast.getY();
			if (newWidth >= stage.getMinWidth()) {
				stage.setX(newX);
				stage.setWidth(newWidth);
				currentX = sizeCurrent.getX();
			}

			if (newHeight >= stage.getMinHeight()) {
				stage.setY(newY);
				stage.setHeight(newHeight);
				currentY = sizeCurrent.getY();
			}
			sizeLast = new Point2D(currentX, currentY);
			// we do not want the title bar getting these events
			// while we are actively resizing
			if (sizeNorth || sizeSouth || sizeWest || sizeEast) {
				event.consume();
			}
		}
	}
}
