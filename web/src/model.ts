export type IInputData = {
  amount: number;
  rate: number;
  years: number;
  overpayment: number;
  grundRate: Grunderwerbsteuer;
  makler: Grunderwerbsteuer;
  landEntry: number;
  notaryFees: number;
  isDetailsOpen: boolean;
};

export type IDetailsData = {
  name: string;
  value: string;
};

export type Grunderwerbsteuer = {
  title: string;
  rate: number;
  rank: number;
  makler?: number;
}

export type Row = {
  cells: Array<Cell>
};

export type Cell = {
  value: number;
  width: number;
  minWidth: number
};

export type TableDataProps = {
  data: Array<Row>;
  isDetailsOpen: boolean;
}
