package org.somox.ejbmox.pcmtx;

import org.somox.ejbmox.pcmtx.model.StatementType;

import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StatementTypeVisitor implements StatementVisitor {

    private StatementType type;

    public StatementType getType() {
        return type;
    }

    @Override
    public void visit(Select arg0) {
        type = StatementType.SELECT;
    }

    @Override
    public void visit(Delete arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Update arg0) {
        type = StatementType.UPDATE;
    }

    @Override
    public void visit(Insert arg0) {
        type = StatementType.INSERT;
    }

    @Override
    public void visit(Replace arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Drop arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Truncate arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(CreateIndex arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(CreateTable arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(CreateView arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Alter arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Statements arg0) {
        throw new UnsupportedOperationException("Compound statements not yet supported.");
    }

    @Override
    public void visit(Execute arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(SetStatement arg0) {
        type = StatementType.OTHER;
    }

    @Override
    public void visit(Merge arg0) {
        type = StatementType.OTHER;
    }

}
