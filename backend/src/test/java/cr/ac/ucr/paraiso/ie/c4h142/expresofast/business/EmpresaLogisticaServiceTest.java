package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.DuplicateResourceException;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaLogisticaService;

    @Test
    void registrarEmpresa_CedulaDuplicada_LanzaExcepcion() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setCedulaJuridica("3-101-123456");

        when(empresaRepository.existsByCedulaJuridica("3-101-123456")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> empresaLogisticaService.registrarEmpresa(empresa));

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void registrarEmpresa_DatosValidos_GuardaYRetornaEmpresa() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setCedulaJuridica("3-101-999999");
        empresa.setNombre("Transportes Rápidos S.A.");

        when(empresaRepository.existsByCedulaJuridica("3-101-999999")).thenReturn(false);
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaLogistica resultado = empresaLogisticaService.registrarEmpresa(empresa);

        assertNotNull(resultado.getFechaRegistro());
        verify(empresaRepository, times(1)).save(any());
    }
}