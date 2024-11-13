package diginamic;

import diginamic.entites.Compte;
import diginamic.entites.Transaction;
import diginamic.exception.BanqueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class BanqueServiceTest {

    @Mock
    CompteDao compteDaoMock;

    @Mock
    TransactionDao transactionDao;

    @Spy
    TransactionProcessor processorMock;

    @InjectMocks
    BanqueService banqueService;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
        Mockito.doNothing().when(processorMock).envoyerMailConfirmation(Mockito.any(Compte.class), Mockito.anyString());
    }

    @Test
    void creerCompteTestWhenCompteExists() throws BanqueException {
        Mockito.when(compteDaoMock.findByNumero("FR16513206")).thenReturn(new Compte());
        assertThrows(BanqueException.class, () -> banqueService.creerCompte("FR16513206",54613.34,"test@test.test"));
    }

    @Test
    void creerCompteTestWhenCompteDoesntExixst(){
        Mockito.when(compteDaoMock.findByNumero(Mockito.anyString())).thenReturn(null);
        try{
            Compte cpt = banqueService.creerCompte("BE165132046549",54613.34,"test@test.test");
            assertEquals("BE165132046549",cpt.getNumero());
            assertEquals(54613.34,cpt.getSolde());
            assertEquals("test@test.test",cpt.getEmail());
        } catch (BanqueException e){
            fail();
        }

    }

    @Test
    void deposerTest() {
        Compte compteMock = new Compte("BE165132046549","test@test.test",64346.64);
        banqueService.deposer(compteMock, 654.0);

        assertEquals(65000.64, compteMock.getSolde());
        assertEquals(0, processorMock.getErrors().size());
    }

    @Test
    void retirerTestSoldeSuffisant() {
        Compte compteMock = new Compte("BE165132046549","test@test.test",64346.64);
        banqueService.retirer(compteMock, 346.64);

        assertEquals(64000.00, compteMock.getSolde());
        assertEquals(0, processorMock.getErrors().size());
    }

    @Test
    void retirerTestSoldeInsuffisant() {
        Compte compteMock = new Compte("BE165132046549","test@test.test",246.64);
        banqueService.retirer(compteMock, 346.64);

        assertEquals(246.64, compteMock.getSolde());
        assertEquals(1, processorMock.getErrors().size());
    }

    @Test
    void virerTestSoldeSuffisant() {
        Compte compteOrigineMock = new Compte("BE165132046549","test@test.test",64346.64);
        Compte compteBeneficaireMock = new Compte("FR16513206","test@test.test",64346.64);
        banqueService.virer(compteOrigineMock,compteBeneficaireMock,346.64);
        assertEquals(63996.5336,compteOrigineMock.getSolde());
        assertEquals(64693.28,compteBeneficaireMock.getSolde());
        assertEquals(0, processorMock.getErrors().size());
    }

    @Test
    void virerTestSoldeInsuffisant(){
        Compte compteOrigineMock = new Compte("BE165132046549","test@test.test",246.64);
        Compte compteBeneficaireMock = new Compte("FR16513206","test@test.test",64346.64);
        banqueService.virer(compteOrigineMock,compteBeneficaireMock,346.64);
        assertEquals(246.64,compteOrigineMock.getSolde());
        assertEquals(64346.64,compteBeneficaireMock.getSolde());
        assertEquals(1, processorMock.getErrors().size());
    }
}