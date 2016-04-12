package container;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Márton Rajnai on 2016-04-04.
 * Contact: marton.rajnai@gmail.com
 */

/**
 * Holds the properties for a player, used for display in a table.
 */
public final class ContainerPlayer {

    private final SimpleStringProperty playerName;
    private final SimpleIntegerProperty playerVote;
    private final SimpleStringProperty playerShapley;
    private final SimpleStringProperty playerBanzhaf;

    public ContainerPlayer() {
        this( "", 0, "", "" );
    }

    /**
     * Create a new player.
     *
     * @param name - player name
     * @param vote - player vote count
     */
    public ContainerPlayer( String name, int vote ) {
        this( name, vote, "", "" );
    }

    /**
     * Create a new player.
     *
     * @param name - player name
     * @param vote - player vote count
     * @param s    - Shapley-Shubik index in string form
     * @param b    - Banzhaf power index in string form
     */
    private ContainerPlayer( String name, int vote, String s, String b ) {
        playerName = new SimpleStringProperty( name );
        playerVote = new SimpleIntegerProperty( vote );
        playerShapley = new SimpleStringProperty( s );
        playerBanzhaf = new SimpleStringProperty( b );
    }

    public String getPlayerName() {
        return playerName.get();
    }

    public int getPlayerVote() {
        return playerVote.get();
    }

    public String getPlayerShapley() {
        return playerShapley.get();
    }

    public String getPlayerBanzhaf() {
        return playerBanzhaf.get();
    }

    public void setPlayerName( String n ) {
        this.playerName.set( n );
    }

    public void setPlayerVote( int v ) {
        this.playerVote.set( v );
    }

    public void setPlayerShapley( String s ) {
        this.playerShapley.set( s );
    }

    public void setPlayerBanzhaf( String b ) {
        this.playerBanzhaf.set( b );
    }
}
