package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BankProfileServiceTest {
    private BankProfileReadingService bankProfileService;

    @Before
    public void setUp(){
        bankProfileService = new BankProfileReadingService();
        bankProfileService.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    public void profileConfigurationWithAdditionalFields() {
        ProfileConfiguration defaultConfiguration = bankProfileService.getProfileConfiguration();

        Whitebox.setInternalState(bankProfileService,
                                  "customBankProfile",
                                  "classpath:bank_profile_additional_fields.yml");


        ProfileConfiguration configurationWithCustomProfile = bankProfileService.getProfileConfiguration();


        assertEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
    public void profileConfigurationWithoutUsualFields() {
        ProfileConfiguration defaultConfiguration = bankProfileService.getProfileConfiguration();

        Whitebox.setInternalState(bankProfileService,
                                  "customBankProfile",
                                  "classpath:bank_profile_missing_fields.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileService.getProfileConfiguration();


        assertNotEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
    public void profileConfigurationScaRedirectFlowOAUTH() {
        //Given
        //When
        Whitebox.setInternalState(bankProfileService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_oauth.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.OAUTH, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }

    @Test
    public void profileConfigurationScaRedirectFlowRedirect() {
        //Given
        //When
        Whitebox.setInternalState(bankProfileService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_redirect.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }
}
