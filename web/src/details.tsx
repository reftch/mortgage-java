import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "./components/ui/accordion";
import { InputGroup, InputGroupAddon, InputGroupInput, InputGroupText } from "./components/ui/input-group";
import { Label } from "./components/ui/label";
import { Menubar, MenubarContent, MenubarItem, MenubarMenu, MenubarTrigger } from "./components/ui/menubar";
import { Separator } from "./components/ui/separator";
import type { Grunderwerbsteuer, IInputData } from "./model";

const GRUNDERWERBSTEUER_LIST: Grunderwerbsteuer[] = [
  { title: "Baden-Württemberg", rate: 5.0, rank: 1, makler: 3.57 },
  { title: "Bayern", rate: 3.5, rank: 2, makler: 3.57 },
  { title: "Berlin", rate: 6.0, rank: 3, makler: 3.57 },
  { title: "Brandenburg", rate: 6.5, rank: 4, makler: 3.57 },
  { title: "Bremen", rate: 5.0, rank: 5, makler: 2.97 },
  { title: "Hamburg", rate: 5.5, rank: 6, makler: 3.12 },
  { title: "Hessen", rate: 6.0, rank: 7, makler: 2.97 },
  { title: "Mecklenburg-Vorprommen", rate: 6.0, rank: 8, makler: 2.97 },
  { title: "Niedersachsen", rate: 5.0, rank: 9, makler: 3.57 },
  { title: "Nordrhein-Westfalen", rate: 6.5, rank: 10, makler: 3.57 },
  { title: "Rheinland-Pfalz", rate: 5.0, rank: 11, makler: 3.57 },
  { title: "Saarland", rate: 6.5, rank: 12, makler: 3.57 },
  { title: "Sachsen", rate: 5.5, rank: 13, makler: 3.57 },
  { title: "Sachsen-Anhalt", rate: 5.0, rank: 14, makler: 3.57 },
  { title: "Schleswig-Holstein", rate: 6.5, rank: 15, makler: 3.57 },
  { title: "Thüringen", rate: 6.5, rank: 16, makler: 3.57 },
].map((f, index) => ({ ...f, rank: index + 1 }));

const UNSELECTED = {
  title: "Nicht Ausgewählt",
  rate: 0.0,
  rank: 0,
  makler: 0.0,
};

export default function Details({ ...props }) {
  const { amount, grundRate, makler, notaryFees, landEntry, isDetailsOpen } = props.v;
  const { overall } = props;

  const handleChange = (field: keyof IInputData, value: number | boolean) => {
    const getNewValue = (field: keyof IInputData, value: number | boolean) => {
      if (field === 'makler') {
        return value === 0 ? UNSELECTED : GRUNDERWERBSTEUER_LIST[(value as number) - 1];
      } else if (field === 'grundRate') {
        return GRUNDERWERBSTEUER_LIST[(value as number) - 1];
      }
      return value;
    };

    props.onChange({
      ...props.v,
      [field]: getNewValue(field, value)
    });
  };

  return (
    <Accordion
      id="details"
      type="single"
      collapsible
      className="w-full px-6"
      defaultValue={isDetailsOpen ? 'item-1' : ''}
      onValueChange={(value) => handleChange('isDetailsOpen', value === 'item-1')}
    >
      <AccordionItem value="item-1">
        <AccordionTrigger>
          <div>
            <b>Nettodarlehen:</b> {overall.toFixed(0)}€
            <b className="pl-3">Über:</b> {(overall - amount).toFixed(0)}€
            <b className="pl-3">Kaufnebenkosten:</b> {((amount * (grundRate.rate + landEntry + notaryFees + makler.makler!)) / 100.0).toFixed(0)}€
          </div>
        </AccordionTrigger>
        <AccordionContent className="pb">
          <Separator className="w-full mt-4" />
          <div className="grid md:grid-cols-10 grid-cols-2 w-full gap-4 text-balance">

            <div className="md:col-span-3 w-full items-center pt-4">
              <Label htmlFor="grund-rate" className="pb-2">Grunderwerbsteuer (required)</Label>
              <InputGroup className="rounded">
                <InputGroupInput id="grund-rate" readOnly value={grundRate.rate.toFixed(1)} />
                <InputGroupAddon>
                  <InputGroupText>%</InputGroupText>
                </InputGroupAddon>
                <InputGroupAddon align="end">
                  <Menubar>
                    <MenubarMenu>
                      <MenubarTrigger className="cursor-pointer">
                        {grundRate.title}
                      </MenubarTrigger>
                      <MenubarContent className="rounded">
                        {GRUNDERWERBSTEUER_LIST.map((g) => (
                          <MenubarItem key={g.rank}
                            className="flex justify-between cursor-pointer"
                            onClick={() => handleChange('grundRate', g.rank)}
                          >
                            <div>{g.title}</div>
                            <div>{`${Number(g.rate).toFixed(1)}%`}</div>
                          </MenubarItem>
                        ))}
                      </MenubarContent>
                    </MenubarMenu>
                  </Menubar>
                </InputGroupAddon>
              </InputGroup>
            </div>

            <div className="md:col-span-2 w-full items-center pt-4">
              <Label htmlFor="notary-fees" className="pb-2">
                Notarkosten, {((amount * notaryFees) / 100).toFixed(0)} €
              </Label>
              <InputGroup className="rounded pb-0">
                <InputGroupAddon>
                  <InputGroupText>%</InputGroupText>
                </InputGroupAddon>
                <InputGroupInput
                  id="notary-fees"
                  value={notaryFees.toFixed(2)}
                  min="0"
                  step="0.05"
                  type="number"
                  onChange={(e: any) => handleChange('notaryFees', Number(e.target.value))}
                />
                <InputGroupAddon align="inline-end">
                  <InputGroupText>Prozent</InputGroupText>
                </InputGroupAddon>
              </InputGroup>
            </div>

            <div className="md:col-span-2 w-full items-center pt-4">
              <Label htmlFor="land-entry" className="pb-2">
                Grundbucheintrag, {((amount * landEntry) / 100).toFixed(0)} €
              </Label>
              <InputGroup className="rounded pb-0">
                <InputGroupAddon>
                  <InputGroupText>%</InputGroupText>
                </InputGroupAddon>
                <InputGroupInput
                  id="land-entry"
                  value={landEntry.toFixed(2)}
                  min="0"
                  step="0.05"
                  type="number"
                  onChange={(e: any) => handleChange('landEntry', Number(e.target.value))}
                />
                <InputGroupAddon align="inline-end">
                  <InputGroupText>Prozent</InputGroupText>
                </InputGroupAddon>
              </InputGroup>
            </div>

            <div className="md:col-span-3 w-full items-center pt-4">
              <Label htmlFor="grund-rate" className="pb-2">Maklerprovision, 0€</Label>
              <InputGroup className="rounded">
                <InputGroupInput id="grund-rate" readOnly value={makler.makler!.toFixed(2)} />
                <InputGroupAddon>
                  <InputGroupText>%</InputGroupText>
                </InputGroupAddon>
                <InputGroupAddon align="end">
                  <Menubar>
                    <MenubarMenu>
                      <MenubarTrigger className="cursor-pointer">{makler.title}</MenubarTrigger>
                      <MenubarContent className="rounded">
                        <MenubarItem
                          key={UNSELECTED.rank}
                          className="flex justify-between cursor-pointer"
                          onClick={() => handleChange('makler', UNSELECTED.rank)}
                        >
                          <div>{UNSELECTED.title}</div>
                          <div>{`${Number(UNSELECTED.makler).toFixed(1)}%`}</div>
                        </MenubarItem>
                        {GRUNDERWERBSTEUER_LIST.map((g) => (
                          <MenubarItem
                            key={g.rank}
                            className="flex justify-between cursor-pointer"
                            onClick={() => handleChange('makler', g.rank)}
                          >
                            <div>{g.title}</div>
                            <div>{`${Number(g.makler).toFixed(2)}%`}</div>
                          </MenubarItem>
                        ))}
                      </MenubarContent>
                    </MenubarMenu>
                  </Menubar>
                </InputGroupAddon>
              </InputGroup>
            </div>

          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}

