import control.ControlDefaultView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Márton Rajnai on 2016-04-03.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Main class that is the starting point for the application.
 */
public class PowerIndexMain extends Application {

    /**
     * Creates the main Scene from the specified FXML file.
     *
     * @return Scene - the main Scene of the application
     * @throws IOException - if the FXML was not found.
     */
    private Scene createMainScene() throws IOException {
        FXMLLoader mainLoader = new FXMLLoader( this.getClass().getResource( "/fxml/DefaultView.fxml" ) );
        mainLoader.setController( new ControlDefaultView() );
        return new Scene( mainLoader.load() );
    }

    /**
     * Initializes the application Stage, add title, and an icon.
     *
     * @param primaryStage - the Stage of the application
     * @throws Exception
     */
    private void setUpMainStage( Stage primaryStage ) throws Exception {
        primaryStage.setTitle( "PICalc" );
        primaryStage.getIcons().add( new Image( "/graphics/pi_icon64.png" ) );
        primaryStage.setScene( createMainScene() );
        primaryStage.setOnCloseRequest( event -> Platform.exit() );
    }

    /**
     * The start of the application, shows the main Stage.
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start( Stage primaryStage ) throws Exception {
        setUpMainStage( primaryStage );
        primaryStage.show();
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
