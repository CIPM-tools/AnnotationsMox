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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.somox.ejbmox.inspectit2pcm.InspectIT2PCMConfiguration;
import org.somox.ejbmox.inspectit2pcm.jobs.InspectIT2PCMConfigurationAttributes;

public class InspectIT2PCMTab extends AbstractLaunchConfigurationTab {

	private Text txtCmrUrl;
	private Text txtWarmup;

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {
		final ModifyListener modifyListener = new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		};

		final Composite container = new Composite(parent, SWT.NONE);
		this.setControl(container);
		container.setLayout(new GridLayout());

		Group grpInspectitCmr = new Group(container, SWT.NONE);
		grpInspectitCmr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		grpInspectitCmr.setText("InspectIT CMR");
		grpInspectitCmr.setLayout(new GridLayout(3, false));

		Label lblNewLabel = new Label(grpInspectitCmr, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("URL to CMR REST API:");

		txtCmrUrl = new Text(grpInspectitCmr, SWT.BORDER);
		txtCmrUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtCmrUrl.addModifyListener(modifyListener);

		Button btnNewButton = new Button(grpInspectitCmr, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtCmrUrl.setText(InspectIT2PCMConfiguration.CMR_REST_API_DEFAULT);
			}
		});
		btnNewButton.setText("Default");

		Group grpSeffParametrization = new Group(container, SWT.NONE);
		grpSeffParametrization.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSeffParametrization.setText("SEFF Parametrization");
		grpSeffParametrization.setLayout(new GridLayout(3, false));

		Label lblWarmupPhase = new Label(grpSeffParametrization, SWT.NONE);
		lblWarmupPhase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblWarmupPhase.setText("Discard initial measurements (warmup phase):");

		txtWarmup = new Text(grpSeffParametrization, SWT.BORDER);
		txtWarmup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtWarmup.addModifyListener(modifyListener);

		Label lblMeasurements = new Label(grpSeffParametrization, SWT.NONE);
		lblMeasurements.setText("measurements");

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL,
				InspectIT2PCMConfiguration.CMR_REST_API_DEFAULT);
		configuration.setAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS,
				InspectIT2PCMConfiguration.WARMUP_MEASUREMENTS_DEFAULT.toString());
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			txtCmrUrl.setText(configuration.getAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL,
					InspectIT2PCMConfiguration.CMR_REST_API_DEFAULT));
			txtWarmup.setText(configuration.getAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS,
					InspectIT2PCMConfiguration.WARMUP_MEASUREMENTS_DEFAULT.toString()));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL, txtCmrUrl.getText());
		configuration.setAttribute(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS, txtWarmup.getText());
	}

	@Override
	public String getName() {
		return "InspectIT-2-PCM";
	}
}
