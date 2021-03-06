package org.annotationsmox.pcmtx.workflow;

import java.util.List;
import java.util.Map;

import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.annotationsmox.pcmtx.SQLHelper;
import org.annotationsmox.pcmtx.model.ParsedSQLStatement;
import org.annotationsmox.pcmtx.model.StatementType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.seff.InternalAction;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import net.sf.jsqlparser.JSQLParserException;

public class ParseSQLJob extends AbstractPCMTXJob {

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        Map<InternalAction, SQLStatement> internalAction2Stmts = getII2PCMPartition().getTrace()
                .getInternalActionToStatementLinks();
        for (SQLStatement stmt : internalAction2Stmts.values()) {
            try {
                StatementType type = SQLHelper.getStatementType(stmt);
                List<String> tableNames = SQLHelper.findTableNames(stmt);
                ParsedSQLStatement parsedStmt = new ParsedSQLStatement(type, tableNames);
                getPCMTXPartition().addParsedStatement(stmt, parsedStmt);
            } catch (JSQLParserException e) {
                logger.error("Could not parse SQL statement: " + stmt, e);
            }
        }
    }

    @Override
    public String getName() {
        return "Parse SQL Statements";
    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
        // nothing to do
    }

}
