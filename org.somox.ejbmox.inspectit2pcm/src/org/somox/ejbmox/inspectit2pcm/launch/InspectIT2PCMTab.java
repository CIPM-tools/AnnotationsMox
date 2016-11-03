package org.somox.ejbmox.inspectit2pcm.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.somox.ui.runconfig.tabs.ModelAnalyzerStrategySelectionTab;

public class InspectIT2PCMTab extends AbstractLaunchConfigurationTab {

    private Text txtCmrUrl;
    private Text txtWarmup;
    private Button btnInternalActionsBeforeStop;
    private Button btnSQLRefinement;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(final Composite parent) {
        final ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                InspectIT2PCMTab.this.setDirty(true);
                InspectIT2PCMTab.this.updateLaunchConfigurationDialog();
            }
        };

        final Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());

        final Group grpInspectitCmr = new Group(container, SWT.NONE);
        grpInspectitCmr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpInspectitCmr.setText("InspectIT CMR");
        grpInspectitCmr.setLayout(new GridLayout(3, false));

        final Label lblNewLabel = new Label(grpInspectitCmr, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel.setText("URL to CMR REST API:");

        this.txtCmrUrl = new Text(grpInspectitCmr, SWT.BORDER);
        this.txtCmrUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.txtCmrUrl.addModifyListener(modifyListener);

        final Button btnNewButton = new Button(grpInspectitCmr, SWT.NONE);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                InspectIT2PCMTab.this.txtCmrUrl.setText(II2PCMConfiguration.CMR_REST_API_DEFAULT);
            }
        });
        btnNewButton.setText("Default");

        final Group grpSeffParametrization = new Group(container, SWT.NONE);
        grpSeffParametrization.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpSeffParametrization.setText("SEFF Parametrization");
        grpSeffParametrization.setLayout(new GridLayout(3, false));

        final Label lblWarmupPhase = new Label(grpSeffParametrization, SWT.NONE);
        lblWarmupPhase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblWarmupPhase.setText("Discard initial measurements (warmup phase):");

        this.txtWarmup = new Text(grpSeffParametrization, SWT.BORDER);
        this.txtWarmup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.txtWarmup.addModifyListener(modifyListener);

        final Label lblMeasurements = new Label(grpSeffParametrization, SWT.NONE);
        lblMeasurements.setText("measurements");

        final Group miscellaneousGroup = new Group(container, SWT.None);
        miscellaneousGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        miscellaneousGroup.setText("Miscellaneous");
        final GridLayout miscellaneousGroupLayout = new GridLayout();
        miscellaneousGroup.setLayout(miscellaneousGroupLayout);

        SelectionListener selectionListener = new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                InspectIT2PCMTab.this.setDirty(true);
                InspectIT2PCMTab.this.updateLaunchConfigurationDialog();
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                InspectIT2PCMTab.this.setDirty(true);
                InspectIT2PCMTab.this.updateLaunchConfigurationDialog();
            }

        };

        this.btnInternalActionsBeforeStop = ModelAnalyzerStrategySelectionTab.createAndAddSWTCheckButton(
                miscellaneousGroup, "Ensure InternalAction before StopAction",
                "If set, we create one InternalAction before each StopAction in the SEFFs. This is necessary if SQL injection is used.",
                selectionListener);

        this.btnSQLRefinement = ModelAnalyzerStrategySelectionTab.createAndAddSWTCheckButton(miscellaneousGroup,
                "Create Internal Action for each SQL statement",
                "Refines coarse-grained Internal Actions with a sequence of one more fine-grained internal action, each representing an SQL statement; coarse-grained Internal Actions without SQL statements in their scope remain unchanged",
                selectionListener);
    }

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL,
                II2PCMConfiguration.CMR_REST_API_DEFAULT);
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS,
                II2PCMConfiguration.WARMUP_MEASUREMENTS_DEFAULT.toString());
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION,
                II2PCMConfiguration.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION_DEFAULT);
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS,
                II2PCMConfiguration.REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS_DEFAULT);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            this.txtCmrUrl.setText(configuration.getAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL,
                    II2PCMConfiguration.CMR_REST_API_DEFAULT));
            this.txtWarmup.setText(configuration.getAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS,
                    II2PCMConfiguration.WARMUP_MEASUREMENTS_DEFAULT.toString()));
            this.btnInternalActionsBeforeStop.setSelection(configuration.getAttribute(
                    InspectIT2PCMConfigurationAttributes.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION,
                    II2PCMConfiguration.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION_DEFAULT));
            this.btnSQLRefinement.setSelection(configuration.getAttribute(
                    InspectIT2PCMConfigurationAttributes.REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS,
                    II2PCMConfiguration.REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS_DEFAULT));
        } catch (final CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL, this.txtCmrUrl.getText());
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS, this.txtWarmup.getText());
        final boolean ensureInternalActions = this.btnInternalActionsBeforeStop.getSelection();
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION,
                ensureInternalActions);
        configuration.setAttribute(InspectIT2PCMConfigurationAttributes.REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS,
                btnSQLRefinement.getSelection());
    }

    @Override
    public String getName() {
        return "InspectIT-2-PCM";
    }
}
