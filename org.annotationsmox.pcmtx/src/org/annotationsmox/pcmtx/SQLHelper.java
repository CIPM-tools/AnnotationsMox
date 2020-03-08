package org.annotationsmox.pcmtx;

import java.util.List;

import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.annotationsmox.pcmtx.model.StatementType;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SQLHelper {

    public static List<String> findTableNames(SQLStatement stmt) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(stmt.getSql());
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = null;
        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            tableList = tablesNamesFinder.getTableList(selectStatement);
        } else if (statement instanceof Update) {
            Update updateStatement = (Update) statement;
            tableList = tablesNamesFinder.getTableList(updateStatement);
        } else if (statement instanceof Insert) {
            Insert insertStatement = (Insert) statement;
            tableList = tablesNamesFinder.getTableList(insertStatement);
        } else {
            throw new RuntimeException("Don't know how to handle statements of type: " + statement.getClass());
        }
        return tableList;
    }

    public static StatementType getStatementType(SQLStatement stmt) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(stmt.getSql());
        StatementTypeVisitor visitor = new StatementTypeVisitor();
        statement.accept(visitor);
        return visitor.getType();

    }

}
