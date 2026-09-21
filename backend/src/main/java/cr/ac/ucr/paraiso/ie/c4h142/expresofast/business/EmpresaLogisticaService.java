package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.DuplicateResourceException;

@Service
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public EmpresaLogistica registrarEmpresa(EmpresaLogistica empresa) {
        if (empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())) {
            throw new DuplicateResourceException(
                "Ya existe una empresa con la cédula jurídica " + empresa.getCedulaJuridica());
        }
        empresa.setFechaRegistro(LocalDateTime.now());
        return empresaRepository.save(empresa);
    }
}