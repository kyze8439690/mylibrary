package me.yugy.app.common.database;

@SuppressWarnings("unused")
public class Column {

    public String getColumnName() {
        return mColumnName;
    }

    public Constraint getConstraint() {
        return mConstraint;
    }

    public DataType getDataType() {
        return mDataType;
    }

    public enum Constraint {
        UNIQUE("UNIQUE"),
        NOT("NOT"),
        NULL("NULL"),
        CHECK("CHECK"),
        FOREIGN_KEY("FOREIGN_KEY"),
        PRIMARY_KEY("PRIMARY_KEY"),
        PRIMARY_KEY_AUTOINCREMENT("PRIMARY_KEY_AUTOINCREMENT");

        private String mValue;

        Constraint(String value){
            mValue = value;
        }

        @Override
        public String toString() {
            return mValue;
        }
    }

    public enum DataType{
        NULL, INTEGER, REAL, TEXT, BLOB
    }

    private String mColumnName;
    private Constraint mConstraint;
    private DataType mDataType;

    public Column(String columnName, Constraint constraint, DataType dataType){
        mColumnName = columnName;
        mConstraint = constraint;
        mDataType = dataType;
    }


}
