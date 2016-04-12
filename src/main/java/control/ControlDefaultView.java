package control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;

import java.io.IOException;

/**
 * Created by Márton Rajnai on 2016-04-08.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Default view controller, used to load the FXML view files for the separate tabs.
 */
public final class ControlDefaultView {

    @FXML
    private TabPane mainTabPane;

    public void initialize() {
        mainTabPane.getSelectionModel().clearSelection();
        mainTabPane.getSelectionModel().selectedItemProperty().addListener( ( observable, oldValue, newValue ) -> {
            if ( newValue == null )
                return;
            if ( newValue.getContent() == null ) {
                try {
                    if ( ( newValue.getText() ).equals( "Power Index Calculator" ) ) {
                        FXMLLoader tabLoader = new FXMLLoader(
                                this.getClass().getResource( "/fxml/PowerIndexView.fxml" ) );
                        tabLoader.setController( new ControlPowerIndexView() );
                        newValue.setContent( tabLoader.load() );
                    } else if ( ( newValue.getText() ).equals( "Simulation" ) ) {
                        FXMLLoader tabLoader = new FXMLLoader(
                                this.getClass().getResource( "/fxml/SimulationView.fxml" ) );
                        tabLoader.setController( new ControlSimulation() );
                        newValue.setContent( tabLoader.load() );
                    }
                } catch ( IOException e ) {
                    System.err.println( "Failed to load view file!" );
                }
            }
        } );
        mainTabPane.getSelectionModel().selectFirst();
    }
}
