import { InputGroup, InputGroupAddon, InputGroupText, InputGroupInput } from "./components/ui/input-group";
import { Label } from "./components/ui/label";
import type { IInputData } from "./layout";

export default function InputData({ ...props }) {

  const handleChange = (field: keyof IInputData, value: number) => {
    props.onChange({
      ...props.v,
      [field]: value
    });
  };

  return (
    <div className="grid md:grid-cols-4 grid-cols-2 w-full px-6 gap-4 justify-items-center">
      <div className="grid w-full items-center">
        <Label htmlFor="amount" className="pb-2">Hypothekenschulden</Label>
        <InputGroup className="rounded">
          <InputGroupAddon>
            <InputGroupText>€</InputGroupText>
          </InputGroupAddon>
          <InputGroupInput
            id="amount"
            value={props.v.amount}
            min="10000"
            step="1000"
            type="number"
            onChange={(e: any) => handleChange('amount', Number(e.target.value))}
          />
          <InputGroupAddon align="inline-end">
            <InputGroupText>EURO</InputGroupText>
          </InputGroupAddon>
        </InputGroup>
        <div className="pt-1 text-[13px] opacity-50">Geben Sie den Kapitalbetrag ein</div>
      </div>

      <div className="grid w-full items-center">
        <Label htmlFor="rate" className="pb-2">Zinsen (pflichtfeld)</Label>
        <InputGroup className="rounded pb-0">
          <InputGroupAddon>
            <InputGroupText>%</InputGroupText>
          </InputGroupAddon>
          <InputGroupInput
            id="rate"
            value={props.v.rate.toFixed(2)}
            min="0.5"
            step="0.05"
            type="number"
            onChange={(e: any) => handleChange('rate', Number(e.target.value))}
          />
          <InputGroupAddon align="inline-end">
            <InputGroupText>Prozent</InputGroupText>
          </InputGroupAddon>
        </InputGroup>
        <div className="pt-1 text-[13px] opacity-50">Geben Sie den Zinsen ein</div>
      </div>

      <div className="grid w-full items-center">
        <Label htmlFor="years" className="pb-2">Jahre (pflichtfeld)</Label>
        <InputGroup className="rounded pb-0">
          <InputGroupInput 
            id="years" 
            value={props.v.years}
            min="1" 
            step="1" 
            type="number" 
            onChange={(e: any) => handleChange('years', Number(e.target.value))}
          />
          <InputGroupAddon align="inline-end">
            <InputGroupText>Jahre</InputGroupText>
          </InputGroupAddon>
        </InputGroup>
        <div className="pt-1 text-[13px] opacity-50">Geben Sie den Zinsen ein</div>
      </div>

      <div className="grid w-full items-center">
        <Label htmlFor="overpayment" className="pb-2">Einmalige Überzahlung</Label>
        <InputGroup className="rounded pb-0">
          <InputGroupAddon>
            <InputGroupText>%</InputGroupText>
          </InputGroupAddon>
          <InputGroupInput 
            id="overpayment" 
            value={props.v.overpayment.toFixed(1)} 
            min="0" 
            step="0.1" 
            type="number" 
            onChange={(e: any) => handleChange('overpayment', Number(e.target.value))}
          />
          <InputGroupAddon align="inline-end">
            <InputGroupText>Prozent</InputGroupText>
          </InputGroupAddon>
        </InputGroup>
        <div className="pt-1 text-[13px] opacity-50">Überzahlung des Jahres</div>
      </div>
    </div>
  )
}