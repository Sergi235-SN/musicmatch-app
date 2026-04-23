package com.musicmatch.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.musicmatch.backend.model.City;
import com.musicmatch.backend.model.Instrument;
import com.musicmatch.backend.model.Style;
import com.musicmatch.backend.repository.CityRepository;
import com.musicmatch.backend.repository.InstrumentRepository;
import com.musicmatch.backend.repository.StyleRepository;

import java.util.Arrays;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(InstrumentRepository instrumentRepo,
                               StyleRepository styleRepo,
                               CityRepository cityRepo) {
        return args -> {

            if (instrumentRepo.count() == 0) {
                instrumentRepo.saveAll(Arrays.asList(
                    new Instrument("guitarra electrica"),
                    new Instrument("guitarra acustica"),
                    new Instrument("bajo electrico"),
                    new Instrument("bajo acustico"),
                    new Instrument("bateria"),
                    new Instrument("piano"),
                    new Instrument("piano electrico"),
                    new Instrument("teclado"),
                    new Instrument("sintetizador"),
                    new Instrument("organo"),
                    new Instrument("violin"),
                    new Instrument("viola"),
                    new Instrument("violonchelo"),
                    new Instrument("contrabajo"),
                    new Instrument("arpa"),
                    new Instrument("ukelele"),
                    new Instrument("banjo"),
                    new Instrument("mandolina"),
                    new Instrument("saxofon"),
                    new Instrument("saxofon alto"),
                    new Instrument("saxofon tenor"),
                    new Instrument("saxofon soprano"),
                    new Instrument("trompeta"),
                    new Instrument("trombon"),
                    new Instrument("tuba"),
                    new Instrument("corno frances"),
                    new Instrument("clarinete"),
                    new Instrument("flauta"),
                    new Instrument("flauta travesera"),
                    new Instrument("oboe"),
                    new Instrument("fagot"),
                    new Instrument("armonica"),
                    new Instrument("acordeon"),
                    new Instrument("melodica"),
                    new Instrument("congas"),
                    new Instrument("bongos"),
                    new Instrument("timbales"),
                    new Instrument("cajon"),
                    new Instrument("pandereta"),
                    new Instrument("maracas"),
                    new Instrument("xilofono"),
                    new Instrument("vibrafono"),
                    new Instrument("campanas"),
                    new Instrument("triangulo"),
                    new Instrument("gong"),
                    new Instrument("djembe"),
                    new Instrument("tabla"),
                    new Instrument("didgeridoo"),
                    new Instrument("charango"),
                    new Instrument("balalaika")
                ));
            }

            if (styleRepo.count() == 0) {
                styleRepo.saveAll(Arrays.asList(
                    new Style("rock"), new Style("pop"), new Style("jazz"),
                    new Style("blues"), new Style("reggae"), new Style("ska"),
                    new Style("hip hop"), new Style("rap"), new Style("trap"),
                    new Style("r&b"), new Style("soul"), new Style("funk"),
                    new Style("disco"), new Style("metal"), new Style("heavy metal"),
                    new Style("thrash metal"), new Style("death metal"), new Style("black metal"),
                    new Style("punk"), new Style("punk rock"), new Style("hardcore punk"),
                    new Style("grunge"), new Style("indie"), new Style("indie rock"),
                    new Style("alternativo"), new Style("electronic"), new Style("techno"),
                    new Style("house"), new Style("deep house"), new Style("trance"),
                    new Style("drum and bass"), new Style("dubstep"), new Style("ambient"),
                    new Style("lo-fi"), new Style("synthwave"), new Style("country"),
                    new Style("folk"), new Style("bluegrass"), new Style("flamenco"),
                    new Style("latin"), new Style("salsa"), new Style("bachata"),
                    new Style("merengue"), new Style("cumbia"), new Style("reggaeton"),
                    new Style("bossa nova"), new Style("samba"), new Style("tango"),
                    new Style("clasica"), new Style("opera"), new Style("gospel"),
                    new Style("new age"), new Style("experimental"), new Style("instrumental")
                ));
            }

            if (cityRepo.count() == 0) {
                cityRepo.saveAll(Arrays.asList(
                    new City("Algeciras"),
                    new City("Arcos de la Frontera"),
                    new City("Barbate"),
                    new City("Los Barrios"),
                    new City("Benalup-Casas Viejas"),
                    new City("Cadiz"),
                    new City("Chiclana de la Frontera"),
                    new City("Chipiona"),
                    new City("Conil de la Frontera"),
                    new City("El Puerto de Santa Maria"),
                    new City("Jerez de la Frontera"),
                    new City("La Linea de la Concepcion"),
                    new City("San Fernando"),
                    new City("San Roque"),
                    new City("Tarifa"),
                    new City("Ubrique"),
                    new City("Vejer de la Frontera"),
                    new City("Puerto Real"),
                    new City("Medina Sidonia"),
                    new City("Setenil de las Bodegas"),
                    new City("Rota"),
                    new City("San Martin del Tesorillo"),
                    new City("Trebujena"),
                    new City("San Lucar de Barrameda"),
                    new City("Jimena de la Frontera")
                ));
            }
        };
    }
}
