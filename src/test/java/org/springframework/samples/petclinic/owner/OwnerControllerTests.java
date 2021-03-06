package org.springframework.samples.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.toggles.ABTestingLogger;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(OwnerController.class)
public class OwnerControllerTests {

    private static final int TEST_OWNER_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    private Owner george;

    @Before
    public void setup() {
        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;
        FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH = true;
        
        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
    }

    @Test
    public void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }
    

    @Test
    public void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01316761638")
        )
            .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testInitFindForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testProcessFindFormSuccess() throws Exception {
        given(this.owners.findByLastName("")).willReturn(Lists.newArrayList(george, new Owner()));
        mockMvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormByFirstName() throws Exception {
        given(this.owners.findByFirstName(george.getFirstName())).willReturn(Lists.newArrayList(george));
        mockMvc.perform(get("/owners2")
            .param("firstName", "George")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
    }

    @Test
    public void testProcessFindFormByLastName() throws Exception {
        given(this.owners.findByLastName(george.getLastName())).willReturn(Lists.newArrayList(george));
        mockMvc.perform(get("/owners")
            .param("lastName", "Franklin")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
    }

    @Test
    public void testProcessFindFormNoOwnersFound() throws Exception {
        mockMvc.perform(get("/owners")
            .param("lastName", "Unknown Surname")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("owner", "lastName"))
            .andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testInitUpdateOwnerForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testProcessUpdateOwnerFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01616291589")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessUpdateOwnerFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    public void testShowOwner() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/ownerDetails"));
    }

    @Test
    public void testDO_ENABLE_FIRST_NAME_SEARCHToggle() throws Exception{
        for (int i=0; i<400; i++){
            if (Math.random() < 0.5) {
                ABTestingLogger.log("Search by first name experiment A starts" ,"","a");
                FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH = false;
                testExpAFirstNameSearchToggle();
            }
            else {
                ABTestingLogger.log("Search by first name experiment B starts" ,"","b");
                FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH = true;
                //usage of existing test for this feature
                testProcessFindFormByFirstName();
            }
        }
    }

    @Test
    public void testExpAFirstNameSearchToggle() throws Exception {
        FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH = false;
        //DO_ENABLE_FIRST_NAME_SEARCH toggle is set to false
        given(this.owners.findByFirstName(george.getFirstName())).willReturn(Lists.newArrayList(george));
        mockMvc.perform(get("/owners2")
            .param("firstName", "George")
        )
            .andExpect(view().name("/error"));
    }

    @Test
    public void DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION() throws Exception {
        // Reset logs
        ABTestingLogger.resetLogger();

        // Use Feature A
        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;

        // Execute experiment A
        this.experimentA();

        // Use Feature B
        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = true;

        // Execute experiment B
        this.experimentB();

        // Rollback Feature back to A
        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;

        // Show that feature can be rolled back to experiment A
        this.experimentA();

        // Want to show that you can do random experiments
        for (int i=0; i<400; i++){
            if (Math.random() < 0.5) {
                // Use Feature A
                FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;
                experimentA();
            }
            else {
                FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = true;
                experimentB();
            }
        }
    }

    public void experimentA() throws Exception{
        // Log start of experiment A
        ABTestingLogger.log("Experiment A Start", "", "a");

        // Make post request on /owners/new and check redirect occurs to owner page
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01316761638"))
            .andExpect(status().is(302))
            .andExpect(redirectedUrl("/owners/null"));

        // End experiment A
        ABTestingLogger.log("Experiment A End", "", "a");
    }

    public void experimentB() throws Exception{
        // Start experiment B
        ABTestingLogger.log("Experiment B Start", "", "b");

        // Make post request on /owners/new and check redirect occurs to pet form page
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01316761638"))
            .andExpect(status().is(302))
            .andExpect(redirectedUrl("/owners/null/pets/new"));

        // End experiment B
        ABTestingLogger.log("Experiment B End", "", "b");
    }


    @Test
    public void DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS() throws Exception {
        
        // Reset logs
        ABTestingLogger.resetLogger();

        // Use Feature A
        FeatureToggleManager.DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS = false;

        // Execute experiment A
        this.experimentA_Click_Find_Owner();

        // Use Feature B
        FeatureToggleManager.DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS = true;
         
        // Execute experiment B
        this.experimentB_Click_Find_Owner();

        // Rollback Feature back to A
        FeatureToggleManager.DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS = false;

        // Show that feature can be rolled back to experiment A
        this.experimentA_Click_Find_Owner();
    }



    public void experimentA_Click_Find_Owner()throws Exception{

        ABTestingLogger.log("Experiment A for Click Find Owner Start", "", "b");
        mockMvc.perform(get("/owners/find"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("owner"))
        .andExpect(view().name("owners/findOwners"));
        ABTestingLogger.log("Experiment A for Click Find Owner End", "", "b");

    }

    public void experimentB_Click_Find_Owner()throws Exception{
        ABTestingLogger.log("Experiment B for Click Find Owner Start", "", "b");
        OwnerController.SYSTEM_UNDER_TEST = true;   
        mockMvc.perform(get("/owners/find"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("owner"))
        .andExpect(view().name("owners/ownersList"));
        OwnerController.SYSTEM_UNDER_TEST = false;   
        ABTestingLogger.log("Experiment B for Click Find Owner End", "", "b");

    }





}
