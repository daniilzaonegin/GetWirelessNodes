package ru.msu.cmc.getwirelessnodes.database;

/**
 * Created by Данила on 21.03.2017.
 */

public class NodesDbSchema {
    public static final class NodesTable {

        public static final String NAME ="nodes";

        public static final class Cols {
            public static final String TIMESTAMP = "timestamp";
            public static final String ID = "id";
            public static final String TYPE = "type";
            public static final String NAME = "name";
            public static final String FREQUENCY = "frequency";
            public static final String LEVEL = "level";
            public static final String CAPABILITIES = "CAPABILITIES";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";

        }
    }
}
