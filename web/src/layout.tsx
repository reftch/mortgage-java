import { Card } from "./components/ui/card";
import { Separator } from "./components/ui/separator";
import InputData from "./input-data";
import { signal } from '@preact/signals';
import TableData from "./table-data";

export type IInputData = {
  amount: number;
  rate: number;
  years: number;
  overpayment: number;
};

export type IDetailsData = {
  name: string;
  value: string;
};

const inputData = signal<IInputData>({
  amount: 300000,
  years: 20,
  rate: 3.3,
  overpayment: 5.0,
});

export default function Layout() {
  return (
    <div className="max-w-5xl w-full items-center justify-center md:pt-5">
      <Card className="w-full rounded">
        <InputData {...inputData} onChange={(data: IInputData) => inputData.value = data} />
        <Separator />
        <TableData
          amount={inputData.value.amount}
          years={inputData.value.years}
          rate={inputData.value.rate / 100.0 / 12}
          overpayment={inputData.value.overpayment / 100.0}
        />
      </Card>
    </div>
  )
}