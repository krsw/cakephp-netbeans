/*
 * TODO: add license
 */

package org.cakephp.netbeans;

import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class CakeScript extends PhpProgram {
    public static final String SCRIPT_NAME = "cake"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    private static final String SCRIPT_DIRECTORY = "cake/console/"; // NOI18N
    private static final String CMD_BAKE = "bake"; // NOI18N

    private final PhpModule phpModule;

    private CakeScript(String command) {
        this(command, null);
    }

    private CakeScript(String command, PhpModule phpModule) {
        super(command);
        this.phpModule = phpModule;
    }

    /**
     * Get the project specific, <b>valid only</b> Cake script. If not found, the {@link InvalidPhpProgramException} is thrown.
     * @param phpModule PHP module for which Cake script is taken
     * @return the project specific, <b>valid only</b> Cake script
     * @throws InvalidPhpProgramException if Zend script is not valid or missing completely
     */
    public static CakeScript forPhpModule(PhpModule phpModule) throws InvalidPhpProgramException {
        FileObject sourceDirectory = phpModule.getSourceDirectory();

        // locate
        FileObject cake = sourceDirectory.getFileObject(SCRIPT_DIRECTORY + SCRIPT_NAME);
        if (cake == null) {
            cake = sourceDirectory.getFileObject(SCRIPT_DIRECTORY + SCRIPT_NAME_LONG);
        }
        if (cake == null) {
            throw new InvalidPhpProgramException(NbBundle.getMessage(CakeScript.class, "MSG_CakeNotFound"));
        }

        // validate
        String cakePath = FileUtil.toFile(cake).getAbsolutePath();
        String error = validate(cakePath);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new CakeScript(cakePath, phpModule);
    }

    @Override
    public String validate() {
        // TODO: WARNING: perhaps no reliable way to find out whether the cake command is executable (INFO message to IDE log is written)
        return FileUtils.validateScript(getProgram(), NbBundle.getMessage(CakeScript.class, "LBL_CakeTool"));
    }

    public static String validate(String command) {
        return new CakeScript(command).validate();
    }

    // TODO: later, run it via FrameworkCommandSupport
    public void runBake() {
        ExecutionDescriptor executionDescriptor = getExecutionDescriptor()
                .outProcessorFactory(ANSI_STRIPPING_FACTORY)
                .errProcessorFactory(ANSI_STRIPPING_FACTORY);

        ExternalProcessBuilder processBuilder = getProcessBuilder()
                .addArgument(CMD_BAKE);
        assert phpModule != null;
        if (phpModule != null) {
            processBuilder = processBuilder
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()));
        }
        executeLater(processBuilder, executionDescriptor, CMD_BAKE);
    }
}
