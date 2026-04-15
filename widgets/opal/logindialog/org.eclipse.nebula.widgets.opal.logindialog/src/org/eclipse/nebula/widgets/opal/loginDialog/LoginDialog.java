/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.loginDialog;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.ResourceManager;
import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.nebula.widgets.opal.dialog.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class are Login Dialog box, which is composed of
 * <p>
 * <dl>
 * <dt><b>A login</b></dt>
 * <dt><b>A password</b></dt>
 * <dt><b>An image</b></dt>
 * <dd>(optional)</dd>
 * <dt><b>A description</b></dt>
 * <dd>(optional)</dd>
 * <dt><b>A checkbox "remember the password"</b></dt>
 * <dd>(optional)</dd>
 * </dl>
 * </p>
 */
public class LoginDialog {
	private Image image;
	private String description;
	private String login;
	private String password;
	private List<String> autorizedLogin;
	private boolean displayRememberPassword;
	private boolean rememberPassword;
	private LoginDialogVerifier verifier;

	private Shell shell;
	private boolean returnedValue;
	private Button buttonOk;

	/**
	 * Constructor
	 */
	public LoginDialog() {
		displayRememberPassword = true;
	}

	/**
	 * Open the Login box
	 *
	 * @return <code>true</code> if the authentication is OK, <code>false</code>
	 *         if the user pressed on cancel.
	 */
	public boolean open() {
		if (verifier == null) {
			throw new IllegalArgumentException("Please set a verifier before opening the dialog box");
		}

		buildDialog();
		openShell();

		return returnedValue;
	}

	/**
	 * Build the dialog box
	 */
	private void buildDialog() {
		buildShell();
		buildImage();
		buildDescription();
		buildLogin();
		buildPassword();
		if (displayRememberPassword) {
			buildRememberPassword();
		}
		buildButtons();
	}

	/**
	 * Build the shell
	 */
	private void buildShell() {
		shell = new Shell(SWT.SYSTEM_MODAL | SWT.TITLE | SWT.BORDER);
		shell.setText(ResourceManager.getLabel(ResourceManager.LOGIN));
		shell.setLayout(new GridLayout(4, false));
	}

	/**
	 * Build the image on top of the login box. If no image has been set, create
	 * a default image
	 */
	private void buildImage() {
		final Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		final GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1);
		gridData.widthHint = 400;
		gridData.heightHint = 60;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(e -> {
			e.gc.drawImage(image == null ? createDefaultImage(e.width, e.height) : image, 0, 0);
		});

	}

	/**
	 * Create a default image. It is a port of the image used by the Login Box
	 * in the project SwingX
	 *
	 * @param w width
	 * @param h height
	 * @return a default image (blue wave)
	 */
	private Image createDefaultImage(final int w, final int h) {
		final Display display = Display.getCurrent();
		final Color backgroundColor = new Color(49, 121, 242);
		final Color gradientColor1 = new Color(155, 185, 245);
		final Color gradientColor2 = new Color(53, 123, 242);

		final Image img = new Image(display, w, h);
		final GC gc = new GC(img);
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		gc.setBackground(backgroundColor);
		gc.fillRectangle(0, 0, w, h);

		final Path curveShape = new Path(display);
		curveShape.moveTo(0, h * .6f);
		curveShape.cubicTo(w * .167f, h * 1.2f, w * .667f, h * -.5f, w, h * .75f);
		curveShape.lineTo(w, h);
		curveShape.lineTo(0, h);
		curveShape.lineTo(0, h * .8f);
		curveShape.close();

		final Pattern pattern = new Pattern(display, 0, 0, 1, h * 1.2f, gradientColor1, gradientColor2);
		gc.setBackgroundPattern(pattern);
		gc.fillPath(curveShape);

		final Font font = new Font(display, "Arial Bold", 30, SWT.NONE);
		gc.setFont(font);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		final Point textSize = gc.stringExtent(ResourceManager.getLabel(ResourceManager.LOGIN));
		gc.drawString(ResourceManager.getLabel(ResourceManager.LOGIN), (int) (w * .05f), (h - textSize.y) / 2, true);

		font.dispose();
		curveShape.dispose();
		pattern.dispose();
		backgroundColor.dispose();
		gradientColor1.dispose();
		gradientColor2.dispose();
		gc.dispose();
		return img;
	}

	/**
	 * Build the description part of the box
	 */
	private void buildDescription() {
		final Label label = new Label(shell, SWT.NONE);
		final GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4, 1);
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 5;
		label.setLayoutData(gridData);
		final Font bold = SWTGraphicUtil.buildFontFrom(label, SWT.BOLD);
		label.setFont(bold);
		SWTGraphicUtil.addDisposer(label, bold);

		if (description == null || description.trim().equals("")) {
			label.setText(" ");
		} else {
			label.setText(description);
		}
	}

	/**
	 * Build the login part of the box
	 */
	private void buildLogin() {
		final Label label = new Label(shell, SWT.NONE);
		final GridData gridData = new GridData(GridData.END, GridData.END, false, false, 1, 1);
		gridData.horizontalIndent = 35;
		gridData.verticalIndent = 15;
		label.setLayoutData(gridData);
		label.setText(ResourceManager.getLabel(ResourceManager.NAME));

		if (autorizedLogin != null && !autorizedLogin.isEmpty()) {
			// Combo
			buildLoginCombo();
		} else {
			// Text
			buildLoginText();
		}

	}

	private void buildLoginCombo() {
		final Combo combo = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);

		combo.setLayoutData(new GridData(GridData.FILL, GridData.END, true, false, 3, 1));
		for (final String loginToAdd : autorizedLogin) {
			combo.add(loginToAdd);
		}
		combo.setText(login == null ? "" : login);
		combo.setFocus();
		combo.addListener(SWT.Modify, e -> {
			login = combo.getText();
			changeButtonOkState();
		});
	}

	private void buildLoginText() {
		final Text text = new Text(shell, SWT.BORDER);
		text.setText(login == null ? "" : login);
		text.setLayoutData(new GridData(GridData.FILL, GridData.END, true, false, 3, 1));
		text.setFocus();
		text.addListener(SWT.Modify, e -> {
			login = text.getText();
			changeButtonOkState();
		});
	}

	/**
	 * Build the password part of the box
	 */
	private void buildPassword() {
		final Label label = new Label(shell, SWT.NONE);
		final GridData gridData = new GridData(GridData.END, GridData.CENTER, false, false, 1, 1);
		gridData.horizontalIndent = 35;
		label.setLayoutData(gridData);
		label.setText(ResourceManager.getLabel(ResourceManager.PASSWORD));

		final Text text = new Text(shell, SWT.PASSWORD | SWT.BORDER);
		text.setText(password == null ? "" : password);
		text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
		text.addListener(SWT.Modify, e -> {
			password = text.getText();
			changeButtonOkState();
		});
	}

	/**
	 * Enable/Disable the button when the login and the password is empty (or
	 * not)
	 */
	private void changeButtonOkState() {
		final boolean loginEntered = login != null && !login.trim().equals("");
		final boolean passwordEntered = password != null && !password.trim().equals("");
		buttonOk.setEnabled(loginEntered && passwordEntered);
	}

	/**
	 * Build the "remember password" part of the box
	 */
	private void buildRememberPassword() {
		final Button checkbox = new Button(shell, SWT.CHECK);
		final GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 4, 1);
		gridData.horizontalIndent = 35;
		checkbox.setLayoutData(gridData);
		checkbox.setText(ResourceManager.getLabel(ResourceManager.REMEMBER_PASSWORD));
		checkbox.setSelection(rememberPassword);
	}

	/**
	 * Build the buttons
	 */
	private void buildButtons() {
		buildOkButton();
		buildCancelButton();
	}

	private void buildOkButton() {
		buttonOk = new Button(shell, SWT.PUSH);
		final GridData gdOk = new GridData(GridData.END, GridData.CENTER, true, false, 3, 1);
		gdOk.verticalIndent = 60;
		gdOk.minimumWidth = 80;
		buttonOk.setLayoutData(gdOk);
		buttonOk.setText(ResourceManager.getLabel(ResourceManager.OK));
		buttonOk.setEnabled(false);

		buttonOk.addListener(SWT.Selection, event -> {
			try {
				verifier.authenticate(login, password);
				returnedValue = true;
				shell.dispose();
			} catch (final Exception e) {
				Dialog.error(ResourceManager.getLabel(ResourceManager.LOGIN_FAILED), e.getMessage());
				for (final Control control : shell.getChildren()) {
					if (control instanceof Text || control instanceof Combo) {
						control.setFocus();
						break;
					}
				}
			}
		});
	}

	private void buildCancelButton() {
		final Button buttonCancel = new Button(shell, SWT.PUSH);
		final GridData gdCancel = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gdCancel.widthHint = 80;
		gdCancel.verticalIndent = 60;
		buttonCancel.setLayoutData(gdCancel);
		buttonCancel.setText(ResourceManager.getLabel(ResourceManager.CANCEL));
		buttonCancel.addListener(SWT.Selection, event -> {
			returnedValue = false;
			shell.dispose();
		});
	}

	/**
	 * Open the shell
	 */
	private void openShell() {
		shell.setDefaultButton(buttonOk);
		shell.pack();
		shell.open();
		SWTGraphicUtil.centerShell(shell);
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
	}

	// ------------- Getters & Setters
	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login == null ? null : login.trim();
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password == null ? null : password.trim();
	}

	/**
	 * @return the list of autorized logins
	 */
	public List<String> getAutorizedLogin() {
		return autorizedLogin;
	}

	/**
	 * @return <code>true</code> if the checkbox "remember the password" is
	 *         displayed, <code>false</code> otherwise
	 */
	public boolean isDisplayRememberPassword() {
		return displayRememberPassword;
	}

	/**
	 * @return <code>true</code> if the checkbox "remember the password" is
	 *         checked, <code>false</code> otherwise
	 */
	public boolean isRememberPassword() {
		return rememberPassword;
	}

	/**
	 * @return the verifier associated to this box
	 */
	public LoginDialogVerifier getVerifier() {
		return verifier;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(final Image image) {
		this.image = image;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @param autorizedLogin the list of autorized logins to set
	 */
	public void setAutorizedLogin(final List<String> autorizedLogin) {
		this.autorizedLogin = autorizedLogin;
	}

	/**
	 * @param autorizedLogin the list of autorized logins to set
	 */
	public void setAutorizedLogin(final String... autorizedLogin) {
		this.autorizedLogin = Arrays.asList(autorizedLogin);
	}

	/**
	 * @param displayRememberPassword if <code>true</code>, the checkbox
	 *            "remember the password" is displayed
	 */
	public void setDisplayRememberPassword(final boolean displayRememberPassword) {
		this.displayRememberPassword = displayRememberPassword;
	}

	/**
	 * @param rememberPassword if <code>true</code>, the checkbox
	 *            "remember the password" is selected
	 */
	public void setRememberPassword(final boolean rememberPassword) {
		this.rememberPassword = rememberPassword;
	}

	/**
	 * @param verifier the verifier to set
	 */
	public void setVerifier(final LoginDialogVerifier verifier) {
		this.verifier = verifier;
	}

}
